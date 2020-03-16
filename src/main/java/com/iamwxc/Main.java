package com.iamwxc;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Class description goes here.
 * <p>
 * If you see this sentence, nothing ambiguous.
 * </p>
 *
 * @author CC
 * @version 1.0
 */
public class Main implements Runnable {

    // The window handle
    private long window;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(720, 720, "simple", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void update() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glColor3f(1.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glOrtho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
    }

    /**
     * recursive generation triangle
     * @param v first vertex
     * @param v1 second vertex
     * @param v2 third vertex
     * @param v3 forth vertex
     * @param n recursive times
     */
    private void divideTetra(float[] v, float[] v1, float[] v2, float[] v3, int n) {
        if (n <= 0) {
            glColor3f(1.0f, 0.0f, 0.0f);
            glBegin(GL_POLYGON);
            glVertex3fv(v);
            glVertex3fv(v1);
            glVertex3fv(v2);
            glEnd();
            glColor3f(0.0f, 1.0f, 0.0f);
            glBegin(GL_POLYGON);
            glVertex3fv(v);
            glVertex3fv(v1);
            glVertex3fv(v3);
            glEnd();
            glColor3f(0.0f, 0.0f, 1.0f);
            glBegin(GL_POLYGON);
            glVertex3fv(v);
            glVertex3fv(v2);
            glVertex3fv(v3);
            glEnd();
            glColor3f(0.0f, 0.0f, 0.0f);
            glBegin(GL_POLYGON);
            glVertex3fv(v1);
            glVertex3fv(v2);
            glVertex3fv(v3);
            glEnd();
            return;
        }
        float[] a = new float[3];
        float[] b = new float[3];
        float[] c = new float[3];
        float[] d = new float[3];
        float[] e = new float[3];
        float[] f = new float[3];
        for (int i =0; i < 3; i++) {
            a[i] = (float) ((v[i] + v1[i]) / 2.0);
            b[i] = (float) ((v1[i] + v2[i]) / 2.0);
            c[i] = (float) ((v2[i] + v[i]) / 2.0);
            d[i] = (float) ((v[i] + v3[i]) / 2.0);
            e[i] = (float) ((v1[i] + v3[i]) / 2.0);
            f[i] = (float) ((v2[i] + v3[i]) / 2.0);
        }
        divideTetra(v, a, c, d, n - 1);
        divideTetra(a, v1, b, e, n - 1);
        divideTetra(c, b, v2, f, n - 1);
        divideTetra(d, e, f, v3, n - 1);
    }

    private void render() {
        float[][] vertices = new float[4][3];
        vertices[0][0] = 0.0f; vertices[0][1] = 0.0f; vertices[0][2] = 1.0f;
        vertices[1][0] = 0.0f; vertices[1][1] = 0.942809f; vertices[1][2] = -0.33333f;
        vertices[2][0] = -0.816497f; vertices[2][1] = -0.471405f; vertices[2][2] = -0.333333f;
        vertices[3][0] = 0.816497f; vertices[3][1] = -0.471405f; vertices[3][2] = -0.333333f;
        divideTetra(vertices[0], vertices[1], vertices[2], vertices[3], 3);
        glFlush();
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            update();
            render();
            glfwSwapBuffers(window);    // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

}
