package com.ejemplo.reservas;

import com.ejemplo.reservas.repository.ReservaRepository;
import com.ejemplo.reservas.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CrudApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long usuarioId;

    @Test
    void sirveElPanelWebDesdeLaRaiz() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Panel web para probar la API")))
            .andExpect(content().string(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("DBeaver"))));
    }

    @BeforeEach
    void crearUsuarioBase() throws Exception {
        reservaRepository.deleteAll();
        usuarioRepository.deleteAll();
        String respuesta = mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana","correo":"ana@example.com","edad":30}
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode json = objectMapper.readTree(respuesta);
        usuarioId = json.get("id").asLong();
    }

    @Test
    void completaCrudDeUsuarioYReserva() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/usuarios/{id}", usuarioId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correo").value("ana@example.com"));

        mockMvc.perform(put("/api/usuarios/{id}", usuarioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Ana Ruiz","correo":"ana.ruiz@example.com","edad":31}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Ana Ruiz"));

        String reservaRespuesta = mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fechaHora":"2026-10-12T19:30:00","cantidadPersonas":4,
                     "observaciones":"Mesa junto a la ventana","usuarioId":%d}
                    """.formatted(usuarioId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.usuarioId").value(usuarioId))
            .andReturn()
            .getResponse()
            .getContentAsString();
        Long reservaId = objectMapper.readTree(reservaRespuesta).get("id").asLong();

        mockMvc.perform(get("/api/reservas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/reservas").param("usuarioId", usuarioId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/usuarios/{id}/reservas", usuarioId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/reservas/{id}", reservaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cantidadPersonas").value(4));

        mockMvc.perform(put("/api/reservas/{id}", reservaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fechaHora":"2026-10-13T20:00:00","cantidadPersonas":2,
                     "observaciones":"Actualizada","usuarioId":%d}
                    """.formatted(usuarioId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cantidadPersonas").value(2));

        mockMvc.perform(delete("/api/reservas/{id}", reservaId))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/reservas/{id}", reservaId))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/usuarios/{id}", usuarioId))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/usuarios/{id}", usuarioId))
            .andExpect(status().isNotFound());
    }

    @Test
    void rechazaCorreoDuplicado() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Otra Ana","correo":"ana@example.com","edad":25}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void permiteCambiarElUsuarioDeUnaReserva() throws Exception {
        String usuarioRespuesta = mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"Luis","correo":"luis@example.com","edad":28}
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        Long nuevoUsuarioId = objectMapper.readTree(usuarioRespuesta).get("id").asLong();

        String reservaRespuesta = mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fechaHora":"2026-10-12T19:30:00","cantidadPersonas":2,
                     "observaciones":"Reserva","usuarioId":%d}
                    """.formatted(usuarioId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        Long reservaId = objectMapper.readTree(reservaRespuesta).get("id").asLong();

        mockMvc.perform(put("/api/reservas/{id}", reservaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fechaHora":"2026-10-12T19:30:00","cantidadPersonas":2,
                     "observaciones":"Reserva","usuarioId":%d}
                    """.formatted(nuevoUsuarioId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuarioId").value(nuevoUsuarioId));
        mockMvc.perform(get("/api/reservas").param("usuarioId", usuarioId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/reservas").param("usuarioId", nuevoUsuarioId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void eliminarUsuarioEliminaSusReservas() throws Exception {
        String reservaRespuesta = mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fechaHora":"2026-10-12T19:30:00","cantidadPersonas":2,
                     "observaciones":"Reserva","usuarioId":%d}
                    """.formatted(usuarioId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        Long reservaId = objectMapper.readTree(reservaRespuesta).get("id").asLong();

        mockMvc.perform(delete("/api/usuarios/{id}", usuarioId))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/reservas/{id}", reservaId))
            .andExpect(status().isNotFound());
    }

    @Test
    void validaLosDatosDeEntrada() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nombre":"","correo":"correo-invalido","edad":-1}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
