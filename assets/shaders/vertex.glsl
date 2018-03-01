#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 textureCoordinate;
layout(location = 2) in vec3 normal;

//uniforms
uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 proj_matrix;

//light
uniform vec3 lightPosition;
uniform float flashlightFactor;

out struct VertexData
{
    vec3 toLight;
    vec3 toCamera;
    vec2 textureCoordinate;
    vec3 normal;
} vertexData;

void main(){
    //TODO: prepare Phong here

    mat4 model_view = view_matrix * model_matrix;
    mat4 normMat = transpose(inverse(model_view));
    vertexData.normal = vec3(normMat * vec4(normal, 0.0f));

    vec4 viewPos = model_view * vec4(position, 1.0f);
    vertexData.toCamera = -viewPos.xyz;

    vec4 light = view_matrix * vec4(lightPosition, 1.0f) * (1.0f - flashlightFactor);
    vertexData.toLight = (light-viewPos).xyz;

    vec4 pos = proj_matrix * viewPos;
    gl_Position = pos;
    vertexData.textureCoordinate = textureCoordinate;

}