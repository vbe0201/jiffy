package io.github.vbe0201.jiffy.gui

import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import java.io.Closeable

/**
 * The main graphical emulator window.
 *
 * Under the hood, most logic in this class is driven by the GLFW
 * framework, using an OpenGL context for rendering.
 *
 * Most properties accesses and methods resort to GLFW calls, so
 * the user must make sure that the entire creation and handling
 * of the window is done on the main thread of the application.
 *
 * The [Closeable] interface implemented for this class enables
 * neat cleanup of all occupied resources.
 */
class EmulatorWindow : Closeable {
    /** The underlying window handle. */
    private var handle = NULL

    /** The currently active title of the window. */
    var title = "jiffy"
        set(value) {
            if (field == value) {
                return
            }

            glfwSetWindowTitle(this.handle, value)
            field = value
        }

    /** Gets the time since GLFW initialization in seconds. */
    val time: Double
        get() = glfwGetTime()

    /** The string version of the GLFW working behind the window. */
    val version: String = glfwGetVersionString()

    init {
        // Set up an error callback that prints to stderr.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize and configure GLFW.
        check(glfwInit()) {
            close()
            "Unable to initialize GLFW"
        }
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        // Create the window.
        this.handle = glfwCreateWindow(640, 480, this.title, NULL, NULL)
        check(this.handle != NULL) {
            close()
            "Failed to create the window"
        }

        // Make the OpenGL context current and enable V-Sync.
        glfwMakeContextCurrent(this.handle)
        glfwSwapInterval(1)

        // Make the window visible.
        glfwShowWindow(this.handle)
    }

    override fun close() {
        // Free the window callbacks and destroy the window.
        if (this.handle != NULL) {
            Callbacks.glfwFreeCallbacks(this.handle)
            glfwDestroyWindow(this.handle)
        }

        // Terminate GLFW and free the error callback.
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    /** Runs the render loop of the window until shutdown. */
    fun run() {
        // This is crucial for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current
        // thread, creates the GLCapabilities instance and makes the
        // OpenGL bindings available for use.
        GL.createCapabilities()

        // Set the clear color.
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Run the rendering loop until the user attempts to exit.
        while (!glfwWindowShouldClose(this.handle)) {
            // Clear the framebuffer.
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // Swap the color buffers.
            glfwSwapBuffers(this.handle)

            // Poll for window events.
            glfwPollEvents()
        }
    }
}
