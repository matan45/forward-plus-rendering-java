package boot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public class Shader {
    private int program;

    public Shader(String computePath) {
        String computeCode = getShaderCode(computePath);
        int compute = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(compute, computeCode);
        glCompileShader(compute);
        checkCompileError(compute);
        program = glCreateProgram();
        glAttachShader(program, compute);
        glLinkProgram(program);
        glDeleteShader(compute);
    }

    public Shader(String vertexPath, String fragmentPath, String geometryPath) {
        program = glCreateProgram();

        String vertexCode = getShaderCode(vertexPath);
        int vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, vertexCode);
        glCompileShader(vertex);
        checkCompileError(vertex);
        glAttachShader(program, vertex);

        String fragmentCode = getShaderCode(fragmentPath);
        int fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, fragmentCode);
        glCompileShader(fragment);
        checkCompileError(fragment);
        glAttachShader(program, fragment);

        if (geometryPath != null) {
            String geometryCode = getShaderCode(geometryPath);

            int geometry = glCreateShader(GL_GEOMETRY_SHADER);
            glShaderSource(geometry, geometryCode);
            glCompileShader(geometry);
            checkCompileError(geometry);
            glAttachShader(program, geometry);

            glDeleteShader(geometry);
        }
        glDeleteShader(vertex);
        glDeleteShader(fragment);
    }

    public void use() {
        glUseProgram(program);
    }

    private void checkCompileError(int shader) {

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shader, 500));
            System.err.println("Could not compile shader");
            System.exit(-1);
        }
    }

    private String getShaderCode(String path) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file");
            e.printStackTrace();
            System.exit(-1);
        }
        return shaderSource.toString();
    }
}
