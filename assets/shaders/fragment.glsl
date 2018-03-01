#version 330 core

//input from vertex shader
in struct VertexData
{
    vec3 toLight;
    vec3 toCamera;
    vec2 textureCoordinate;
    vec3 normal;
} vertexData;


uniform vec2 screensize;
uniform vec3 lightColor;

uniform sampler2D diff;
uniform sampler2D spec;
uniform sampler2D emit;
uniform sampler2D flashlightTex;
uniform float flashlightFactor;
uniform float shininess;
uniform float uvMultiplier;

//fragment shader output
out vec4 color;

void main(){
    //TODO: integrate phong here
    vec3 toLightNorm = normalize(vertexData.toLight);
    vec3 toCameraNorm = normalize(vertexData.toCamera);
    vec3 normalNorm = normalize(vertexData.normal);

    vec3 ambientLight = lightColor * 0.2f;

    vec3 diffuse_term = texture(diff, vertexData.textureCoordinate * uvMultiplier).xyz;
    float cosA = max(0.0f, dot(toLightNorm, normalNorm));

    vec3 reflect = reflect(-toLightNorm, normalNorm);
    float cosB = max(0.0f, dot(reflect, toCameraNorm));
    float cosBK = pow(cosB, shininess);
    vec3 specular_term = texture(spec, vertexData.textureCoordinate * uvMultiplier).xyz;

    vec3 emit_term = mix(texture(emit, vertexData.textureCoordinate * uvMultiplier), vec4(0.0f), flashlightFactor).xyz;

    vec2 tcFlashLight = vec2(gl_FragCoord.x/screensize.x, gl_FragCoord.y/screensize.y);
    float vecLength = length(vertexData.toLight);
    vec3 newLightColor = (mix(vec4(lightColor, 1.0f), texture(flashlightTex, tcFlashLight), flashlightFactor)/(1.0f + 0.5f * vecLength + 0.5f * vecLength*vecLength)).xyz;

    color = vec4(emit_term + ambientLight * diffuse_term + (diffuse_term * cosA + specular_term * cosBK) * newLightColor, 1.0f);
}
