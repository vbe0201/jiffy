package io.github.vbe0201.jiffy.jit.translation

private const val BLACK = true
private const val RED = false

/**
 * A lookup cache for JIT-compiled [Block]s.
 *
 * Usually when running a program, we often end up executing certain
 * code paths multiple times. Therefore, we do not want to recompile
 * code more often that we need to.
 *
 * This class maps translated [Block]s to the MIPS program counter
 * where they occurred in emulated software. If we have one, we can
 * immediately run it; otherwise we need to compile it first.
 *
 * The backing implementation is an Augmented Interval Tree which
 * supports efficient overlap checking of ranges.
 *
 * None of the [BlockCache] methods are considered thread-safe and
 * should not be accessed concurrently without synchronisation.
 */
class BlockCache {
    private data class BlockNode(var block: Block, var parent: BlockNode?) {
        var color: Boolean = BLACK

        var left: BlockNode? = null
        var right: BlockNode? = null

        var start = block.start
        var end = block.start + block.len
        var max = block.start + block.len
    }

    private var root: BlockNode? = null

    /**
     * The total number of cached [Block]s.
     */
    var count: UInt = 0U
        private set

    /**
     * Clears the cache, removing all elements from it.
     */
    fun clear() {
        this.root = null
        this.count = 0U
    }

    /**
     * Indicates if a given address has an associated cache entry.
     */
    fun containsAddress(addr: UInt): Boolean {
        return getNode(addr) != null
    }

    /**
     * Attempts to find a cached [Block] for a given address.
     */
    fun get(addr: UInt): Block? {
        return getNode(addr)?.block
    }

    /**
     * Inserts a given [Block] into the cache, replacing a previous
     * entry for the block's address, if any.
     */
    fun insert(block: Block) {
        bstInsert(block, null)
    }

    /**
     * Inserts a given [Block] into the cache if no entry exists yet,
     * or updates an existing entry's [Block] data.
     *
     * The supplied closure takes the associated MIPS memory address and
     * the [Block] for an existing entry, and should produce a new [Block].
     */
    fun insertOrUpdate(block: Block, update: (UInt, Block) -> Block) {
        bstInsert(block, update)
    }

    /**
     * Removes the entry for a given address from the cache, if present.
     */
    fun remove(addr: UInt) {
        this.count -= bstRemove(addr)
    }

    /**
     * Gets the start addresses of the [Block]s whose start and end keys
     * overlap the given range.
     */
    fun getOverlaps(addr: UInt, size: UInt): List<UInt> {
        val list = emptyList<UInt>().toMutableList()
        getKeys(this.root, addr, addr + size, list)
        return list
    }

    /**
     * Builds a list of all stored [Block]s sorted by their addresses.
     */
    fun asList(): List<Block> {
        val list = ArrayList<Block>(this.count.toInt())
        addToList(this.root, list)
        return list
    }

    private fun colorOf(node: BlockNode?): Boolean {
        return node == null || node.color
    }

    private fun setColor(node: BlockNode?, color: Boolean) {
        if (node != null) {
            node.color = color
        }
    }

    private fun leftOf(node: BlockNode?): BlockNode? {
        return node?.left
    }

    private fun rightOf(node: BlockNode?): BlockNode? {
        return node?.right
    }

    private fun parentOf(node: BlockNode?): BlockNode? {
        return node?.parent
    }

    private fun getKeys(
        node: BlockNode?,
        start: UInt,
        end: UInt,
        list: MutableList<UInt>,
    ) {
        if (node == null || start >= node.max) {
            return
        }

        getKeys(node.left, start, end, list)

        if (end > node.start) {
            if (start < node.end) {
                list.add(node.start)
            }

            getKeys(node.right, start, end, list)
        }
    }

    private fun propagateIncrease(node: BlockNode) {
        val max = node.max
        var ptr = node

        while (ptr.parent != null) {
            ptr = ptr.parent!!

            if (max > ptr.max) {
                ptr.max = max
            } else {
                break
            }
        }
    }

    private fun propagateFull(node: BlockNode) {
        var ptr = node

        while (true) {
            var max = ptr.end

            if (ptr.left != null && ptr.left!!.max > max) {
                max = ptr.left!!.max
            }

            if (ptr.right != null && ptr.right!!.max > max) {
                max = ptr.right!!.max
            }

            ptr.max = max
            ptr = ptr.parent ?: break
        }
    }

    private fun addToList(node: BlockNode?, list: MutableList<Block>) {
        if (node == null) {
            return
        }

        addToList(node.left, list)
        list.add(node.block)
        addToList(node.right, list)
    }

    private fun getNode(key: UInt): BlockNode? {
        var node = this.root
        while (node != null) {
            node = if (key < node.start) {
                node.left
            } else if (key > node.start) {
                node.right
            } else {
                return node
            }
        }

        return null
    }

    private fun bstInsert(
        block: Block,
        update: ((UInt, Block) -> Block)?
    ) {
        var parent: BlockNode? = null
        var node: BlockNode? = this.root

        while (node != null) {
            parent = node

            node = if (block.start < node.start) {
                node.left
            } else if (block.start > node.start) {
                node.right
            } else {
                if (update != null) {
                    node.block = update(block.start, node.block)

                    val end = block.start + block.len
                    if (end > node.end) {
                        node.end = end
                        if (end > node.max) {
                            node.max = end
                            propagateIncrease(node)
                            restoreBalanceAfterInsertion(node)
                        }
                    } else if (end < node.end) {
                        node.end = end
                        propagateFull(node)
                    }
                }

                return
            }
        }

        val newNode = BlockNode(block, parent)
        if (newNode.parent == null) {
            this.root = newNode
        } else if (newNode.start < parent!!.start) {
            parent.left = newNode
        } else {
            parent.right = newNode
        }

        propagateIncrease(newNode)
        ++this.count
        restoreBalanceAfterInsertion(newNode)
    }

    private fun bstRemove(key: UInt): UInt {
        val node = getNode(key) ?: return 0U

        val replacementNode: BlockNode? =
            if (node.left == null || node.right == null) {
                node
            } else {
                predecessorOf(node)
            }

        val tmp = leftOf(replacementNode) ?: rightOf(replacementNode)
        if (tmp != null) {
            tmp.parent = parentOf(replacementNode)
        }

        if (parentOf(replacementNode) == null) {
            this.root = tmp
        } else if (replacementNode == leftOf(parentOf(replacementNode))) {
            parentOf(replacementNode)!!.left = tmp
        } else {
            parentOf(replacementNode)!!.right = tmp
        }

        if (replacementNode != node) {
            node.start = replacementNode!!.start
            node.block = replacementNode.block
            node.end = replacementNode.end
            node.max = replacementNode.max
        }

        propagateFull(replacementNode)

        if (tmp != null && colorOf(replacementNode) == BLACK) {
            restoreBalanceAfterRemoval(tmp)
        }

        return 1U
    }

    private fun maximum(node: BlockNode): BlockNode {
        var ptr = node
        while (ptr.right != null) {
            ptr = ptr.right!!
        }

        return ptr
    }

    private fun predecessorOf(node: BlockNode): BlockNode? {
        var ptr = node

        if (ptr.left != null) {
            return maximum(ptr.left!!)
        }

        var parent = ptr.parent
        while (parent != null && ptr == parent.left) {
            ptr = parent
            parent = parent.parent
        }

        return parent
    }

    private fun rotateLeft(node: BlockNode?) {
        if (node != null) {
            val right = rightOf(node)

            node.right = leftOf(right)
            if (node.right != null) {
                node.right!!.parent = node
            }

            val nodeParent = parentOf(node)

            right!!.parent = nodeParent
            if (nodeParent == null) {
                this.root = right
            } else if (node == leftOf(nodeParent)) {
                nodeParent.left = right
            } else {
                nodeParent.right = right
            }

            right.left = node
            node.parent = right

            propagateFull(node)
        }
    }

    private fun rotateRight(node: BlockNode?) {
        if (node != null) {
            val left = leftOf(node)

            node.left = rightOf(left)
            if (node.left != null) {
                node.left!!.parent = node
            }

            val nodeParent = parentOf(node)

            left!!.parent = nodeParent
            if (nodeParent == null) {
                this.root = left
            } else if (node == rightOf(nodeParent)) {
                nodeParent.right = left
            } else {
                nodeParent.left = left
            }

            left.right = node
            node.parent = left

            propagateFull(node)
        }
    }

    private fun restoreBalanceAfterInsertion(balance: BlockNode?) {
        setColor(balance, RED)

        var ptr = balance
        while (ptr != null && ptr != this.root && colorOf(parentOf(ptr)) == RED) {
            if (parentOf(balance) == leftOf(parentOf(parentOf(ptr)))) {
                val sibling = rightOf(parentOf(parentOf(ptr)))

                if (colorOf(sibling) == RED) {
                    setColor(parentOf(ptr), BLACK)
                    setColor(sibling, BLACK)
                    setColor(parentOf(parentOf(ptr)), RED)
                    ptr = parentOf(parentOf(ptr))
                } else {
                    if (ptr == rightOf(parentOf(ptr))) {
                        ptr = parentOf(ptr)
                        rotateLeft(ptr)
                    }

                    setColor(parentOf(ptr), BLACK)
                    setColor(parentOf(parentOf(ptr)), RED)
                    rotateRight(parentOf(parentOf(ptr)))
                }
            } else {
                val sibling = leftOf(parentOf(parentOf(ptr)))

                if (colorOf(sibling) == RED) {
                    setColor(parentOf(ptr), BLACK)
                    setColor(sibling, BLACK)
                    setColor(parentOf(parentOf(ptr)), RED)
                    ptr = parentOf(parentOf(ptr))
                } else {
                    if (ptr == leftOf(parentOf(ptr))) {
                        ptr = parentOf(ptr)
                        rotateRight(ptr)
                    }

                    setColor(parentOf(ptr), BLACK)
                    setColor(parentOf(parentOf(ptr)), RED)
                    rotateLeft(parentOf(parentOf(ptr)))
                }
            }
        }

        setColor(this.root, BLACK)
    }

    private fun restoreBalanceAfterRemoval(balance: BlockNode?) {
        var ptr = balance
        while (ptr != this.root && colorOf(ptr) == BLACK) {
            if (ptr == leftOf(parentOf(ptr))) {
                var sibling = rightOf(parentOf(ptr))

                if (colorOf(sibling) == RED) {
                    setColor(sibling, BLACK)
                    setColor(parentOf(ptr), RED)
                    rotateLeft(parentOf(ptr))
                    sibling = rightOf(parentOf(ptr))
                }

                if (colorOf(leftOf(sibling)) == BLACK && colorOf(rightOf(sibling)) == BLACK) {
                    setColor(sibling, RED)
                    ptr = parentOf(ptr)
                } else {
                    if (colorOf(rightOf(sibling)) == BLACK) {
                        setColor(leftOf(sibling), BLACK)
                        setColor(sibling, RED)
                        rotateRight(sibling)
                        sibling = rightOf(parentOf(ptr))
                    }

                    setColor(sibling, colorOf(parentOf(ptr)))
                    setColor(parentOf(ptr), BLACK)
                    setColor(rightOf(sibling), BLACK)
                    rotateLeft(parentOf(ptr))

                    ptr = this.root
                }
            } else {
                var sibling = leftOf(parentOf(ptr))

                if (colorOf(sibling) == RED) {
                    setColor(sibling, BLACK)
                    setColor(parentOf(ptr), RED)
                    rotateRight(parentOf(ptr))
                    sibling = leftOf(parentOf(ptr))
                }

                if (colorOf(rightOf(sibling)) == BLACK && colorOf(leftOf(sibling)) == BLACK) {
                    setColor(sibling, RED)
                    ptr = parentOf(ptr)
                } else {
                    if (colorOf(leftOf(sibling)) == BLACK) {
                        setColor(rightOf(sibling), BLACK)
                        setColor(sibling, RED)
                        rotateLeft(sibling)
                        sibling = leftOf(parentOf(ptr))
                    }

                    setColor(sibling, colorOf(parentOf(ptr)))
                    setColor(parentOf(ptr), BLACK)
                    setColor(leftOf(sibling), BLACK)
                    rotateRight(parentOf(ptr))

                    ptr = this.root
                }
            }
        }

        setColor(ptr, BLACK)
    }
}
