package com.academia.auth.Security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.config.TestContainersConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@Transactional
public class SecurityTests {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    private String gerarToken(Usuario usuario) {

        return jwtService.gerarToken(usuario);
    }

    private Usuario criarUsuario() {

        Usuario usuario = Usuario.builder()
                .nome("Saulo")
                .email("saulo@gmail.com")
                .senha("091812")
                .role(RoleUser.ROLE_USER)
                .build();

        return usuario;
    }

    private Authentication criarAuthentication(Usuario usuario) {
       
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
            
        return authentication;
    }

    @Nested
    class AuthenticationTestsToken {
        
        @Test
        void devePermitirAcessoUsuarioComTokenValido() throws Exception {

            Usuario usuario = criarUsuario();
            usuarioRepository.save(usuario);

            String token = gerarToken(usuario);

            mockMvc.perform(
                get("/usuario/me")
                    .header("Authorization", "Bearer " + token)
            ).andExpect(status().isOk());
        }

        @Test
        void deveNaoPermitirAcessoTokenInvalido() throws Exception {

            Usuario usuario = criarUsuario();
            usuarioRepository.save(usuario);

            String token = "token-inválido";

            mockMvc.perform(
                get("/usuario/me")
                    .header("Authorization", "Bearer " + token)
            ).andExpect(status().isUnauthorized());
        }
        
    }

    @Nested
    class AuthorizationTests {

        @Test
        void devePermitirUsuarioRoleIncorreta() throws Exception {

            var usuario = criarUsuario();
            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);
            usuarioRepository.save(usuario);

            var authentication = criarAuthentication(usuario);

            mockMvc.perform(
                get("/mensalidade/buscar")
                .with(authentication(authentication))
            )
            .andExpect(status().isOk());
        }

        @Test
        void deveNaoPermitirUsuarioComRoleCorreta() throws Exception {

            mockMvc.perform(
                get("/mensalidade/buscar")
                .with(user("usuarioUser").roles("USER"))
            ).andExpect(status().isForbidden());
        }

        @Test
        void devePermitirAcessoAEndPointLivre() throws Exception {

            mockMvc.perform(
                post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nome": "teste",
                            "telefone": "(97) 93752-7421",
                            "email": "teste@gmail.com",
                            "senha": "091812"
                        }
                        """)
            )
            .andExpect(status().isCreated());
        }

    }

}
