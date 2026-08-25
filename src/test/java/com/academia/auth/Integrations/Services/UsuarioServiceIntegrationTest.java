package com.academia.auth.Integrations.Services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Usuario.UsuarioAtualizarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioDeletarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.UsuarioService;
import com.academia.auth.config.TestContainersConfig;

@Transactional
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@SpringBootTest
class UsuarioServiceIntegrationTest {
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextoDaAplicacaoDeveSubir() {

    }

    //helpers
    private UsuarioRequestDTO criarUsuarioRequest() {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();

        dto.setNome("Saulo teste");
        dto.setEmail("teste@gmail.com");
        dto.setSenha("091812");

        return dto;
    }

    private Usuario criarUsuario() {

        Usuario usuario = new Usuario();

        usuario.setNome("Saulo teste");
        usuario.setEmail("sauloteste@gmail.com");
        usuario.setRole(RoleUser.ROLE_USER);
        usuario.setSenha("091812");

        return usuario;
    }

    //tests
    @Nested
    class cadastrarUsuarioTest {
     
        @Test
        void deveCadastrarUsuarioComSucesso() {

            UsuarioRequestDTO dto = criarUsuarioRequest();

            UsuarioResponseDTO resultado = usuarioService.cadastrarUsuario(dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo(dto.getEmail());

            Usuario usuarioSalvo = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow();

            assertThat(passwordEncoder.matches(
                dto.getSenha(),
                usuarioSalvo.getSenha() 
            )).isTrue();        

            assertThat(usuarioSalvo.getEmail()).isEqualTo(dto.getEmail());
            assertThat(usuarioSalvo.getSenha()).isNotEqualTo(dto.getSenha());       
        }

        @Test
        void deveLancarExcecaoTemUsuarioCadastradoComEmail() {

            Usuario usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuarioRepository.save(usuario);

            long quantidadeUsuarioAntes = usuarioRepository.count();

            UsuarioRequestDTO dto = criarUsuarioRequest(); 
            dto.setEmail(usuario.getEmail());

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.cadastrarUsuario(dto)
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("Email já cadastrado!");

            long quantidadeUsuarioDepois = usuarioRepository.count();

            assertThat(quantidadeUsuarioAntes).isEqualTo(quantidadeUsuarioDepois);
        }   

        @Test
        void deveComecarComBancoVazio() {

            assertThat(usuarioRepository.count())
                .isZero();
        }

    }

    @Nested
    class atualizarUsuarioTest {
        
        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuario = usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);   
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        // tests
        @Test
        void deveAtualizarUsuarioComSucesso() {

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO(
                "Saulin",
                "saulin@gmail.com",
                "98 94833 9743",
               "091812",
               null 
            );

            UsuarioResponseDTO resultado = usuarioService.atualizarUsuario(dto);

            assertThat(resultado.getEmail()).isEqualTo(dto.getEmail());
            assertThat(resultado.getNome()).isEqualTo(dto.getNome());

            Usuario usuarioAtualizado = usuarioRepository.findById(usuario.getId())
                .orElseThrow();

            assertThat(usuarioAtualizado.getEmail()).isEqualTo(dto.getEmail());
            assertThat(usuarioAtualizado.getNome()).isEqualTo(dto.getNome());

            assertThat(passwordEncoder.matches(
                "091812",
                usuarioAtualizado.getSenha()
            )).isTrue();
        }

        @Test
        void deveAtualizarUsuarioComNovaSenhaComSucesso() {

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO(
                "Saulin",
                "saulin@gmail.com",
                "98 89433 8435",
                "091812",
                "123456"
            );

            UsuarioResponseDTO resultado = usuarioService.atualizarUsuario(dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getEmail()).isEqualTo(dto.getEmail());
            assertThat(resultado.getNome()).isEqualTo(dto.getNome());

            Usuario usuarioAtualizado = usuarioRepository.findById(usuario.getId())
                .orElseThrow();

            assertThat(usuarioAtualizado.getEmail()).isEqualTo(dto.getEmail());
            assertThat(usuarioAtualizado.getNome()).isEqualTo(dto.getNome());

            assertThat(passwordEncoder.matches(
                "091812",
                usuarioAtualizado.getSenha()
            )).isFalse();

            assertThat(passwordEncoder.matches(
                "123456",
                usuarioAtualizado.getSenha()
            )).isTrue();
        }

        @Test
        void deveLancarExcecaoSenhaIncorreta() {

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO(
                "Saulin",
                "saulin@gmail.com",
                "97 84783 9843",
                "091813",
                "123456"
            );

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.atualizarUsuario(dto) 
            );

            assertThat(exception.getMessage()).isEqualTo("Senha incorreta!");
        }

        @Test
        void deveLancarExcecaoUsuarioJaExistenteComEsseEmail() {

            Usuario usuarioExistente = criarUsuario();
            usuarioExistente.setEmail("existe@gmail.com");

            usuarioRepository.save(usuarioExistente);

            UsuarioAtualizarDTO dto = new UsuarioAtualizarDTO(
                "Saulin",
                "existe@gmail.com",
                "98 73932 9847",
                "091812",
                null
            );

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.atualizarUsuario(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Usuário já existente com esse email!");
        }

    }

    @Nested
    class promoverUsuarioAFuncionarioTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuario = usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);   
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void devePromoverUsuarioAFuncionarioComSucesso() {

            usuario.setRole(RoleUser.ROLE_ADMIN);

            Usuario usuarioPromovido = criarUsuario();
            usuarioPromovido.setEmail("usuariopromovido@gmail.com");

            usuarioRepository.save(usuarioPromovido);

            UsuarioResponseDTO resultado = usuarioService.promoverUsuarioAFuncionario(usuarioPromovido.getId());

            assertThat(resultado).isNotNull();

            assertThat(resultado.getEmail()).isEqualTo(usuarioPromovido.getEmail());
            assertThat(resultado.getRole()).isEqualTo(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuarioPromovidoSalvo = usuarioRepository.findById(usuarioPromovido.getId())
                .orElseThrow();

            assertThat(usuarioPromovidoSalvo.getAcessosAcademia()).isNotNull();

            assertThat(usuarioPromovidoSalvo.getId()).isEqualTo(usuarioPromovido.getId());
            assertThat(usuarioPromovidoSalvo.getRole()).isEqualTo(RoleUser.ROLE_FUNCIONARIO);
        }
        
        @Test
        void deveLancarExcecaoVoceNaoTemPermissaoParaPromoverAFuncionario() {

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.promoverUsuarioAFuncionario(2L)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para promover usuário a funcionário!");
        }

        @Test
        void deveLancarExcecaoVoceNaoPodePromoverFuncionariosJaFuncionarios() {

            Usuario usuarioPromovido = criarUsuario();
            usuarioPromovido.setEmail("promovido@gmail.com");
            usuarioPromovido.setRole(RoleUser.ROLE_FUNCIONARIO);

            usuarioRepository.save(usuarioPromovido);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.promoverUsuarioAFuncionario(usuarioPromovido.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para promover funcionários!");
        }

    }
 
    @Nested
    class rebaixarFuncionarioAUsuarioTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveRebaixarFuncionarioAUsuarioComSucesso() {

            Usuario usuarioRebaixado = criarUsuario();
            usuarioRebaixado.setEmail("rebaixado@gmail.com");
            usuarioRebaixado.setRole(RoleUser.ROLE_FUNCIONARIO);

            usuarioRepository.save(usuarioRebaixado);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            UsuarioResponseDTO resultado = usuarioService.rebaixarFuncionarioAUsuario(usuarioRebaixado.getId());

            assertThat(resultado.getEmail()).isEqualTo(usuarioRebaixado.getEmail());
            assertThat(resultado.getRole()).isEqualTo(RoleUser.ROLE_USER);

            Usuario usuarioRebaixadoSalvo = usuarioRepository.findById(usuarioRebaixado.getId())
                .orElseThrow();

            assertThat(usuarioRebaixadoSalvo.getRole()).isEqualTo(RoleUser.ROLE_USER);
        }

        @Test
        void deveLancarExcecaoVoceNaoTemPermissaoParaRebaixarFuncionario() {

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.rebaixarFuncionarioAUsuario(2L)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para rebaixar funcionários!");
        }

        @Test
        void deveLancarExcecaoUsuarioNaoPodeSerRebaixadoAAluno() {

            Usuario funcionarioRebaixado = criarUsuario();
            funcionarioRebaixado.setEmail("rebaixado@gmail.com");
            funcionarioRebaixado.setRole(RoleUser.ROLE_ADMIN);

            usuario.setRole(RoleUser.ROLE_ADMIN);

            usuarioRepository.save(funcionarioRebaixado);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.rebaixarFuncionarioAUsuario(funcionarioRebaixado.getId())
            );

            assertThat(exception.getMessage()).isEqualTo("Este usuário não pode ser rebaixado a um aluno!");
        }

    }
    
    @Nested
    class listarUsuariosTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveListarUsuariosComSucesso() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuario2 = criarUsuario();
            usuario2.setEmail("teste2@gmail.com");
            
            Usuario usuario3 = criarUsuario();
            usuario3.setEmail("teste3@gmail.com");

            List<Usuario> usuarios= List.of(usuario2, usuario3);

            usuarioRepository.saveAll(usuarios);

            Pageable pageable = PageRequest.of(0, 10);

            Page<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios(pageable);

            assertThat(resultado.getContent()).extracting(UsuarioResponseDTO::getId)
                .containsExactlyInAnyOrder(usuario.getId(), usuario2.getId(), usuario3.getId());
        }

        @Test
        void deveLancarExcecaoSemPermissaoParaVisualizarTodosUsuarios() {

            Pageable pageable = PageRequest.of(0, 10);

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.listarUsuarios(pageable)
            );

            assertThat(exception.getMessage()).isEqualTo("Você não tem permissão para ver os usuários!");
        }

    }

    @Nested
    class listarUsuariosPorNomeTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveListarUsuariosPaginadosPorNome() {

            usuario.setRole(RoleUser.ROLE_FUNCIONARIO);

            Usuario usuario2 = criarUsuario();
            usuario2.setEmail("teste1@gmail.com");

            Usuario usuario3 = criarUsuario();
            usuario3.setEmail("teste2@gmail.com");

            List<Usuario> usuarios = List.of(usuario2, usuario3);

            usuarioRepository.saveAll(usuarios);

            Pageable pageable = PageRequest.of(0, 10);

            Page<UsuarioResponseDTO> resultado = usuarioService.listarUsuariosPorNome(
                "sau", 
                pageable
            );

            assertThat(resultado).isNotEmpty();

            assertThat(resultado.getContent()).extracting(UsuarioResponseDTO::getId)
                .containsExactlyInAnyOrder(usuario.getId(), usuario2.getId(), usuario3.getId());

            Page<Usuario> usuariosSalvos = usuarioRepository.findAllByNomeContainingIgnoreCase("sau", pageable);

            assertThat(usuariosSalvos.getContent()).extracting(Usuario::getId)
                .containsExactlyInAnyOrder(usuario.getId(), usuario2.getId(), usuario3.getId());
        }

    }

    @Nested
    class listarMeusDadosTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveListarMeusDadosComSucesso() {

            UsuarioResponseDTO resultado = usuarioService.meusDados();

            assertThat(resultado.getId()).isEqualTo(usuario.getId());
            assertThat(resultado.getEmail()).isEqualTo(usuario.getEmail());
            assertThat(resultado.getNome()).isEqualTo(usuario.getNome());
        }   

    }

    @Nested
    class deletarUsuarioTest {

        private Usuario usuario;

        @BeforeEach
        void configurarUsuarioAutenticado() {

            usuario = criarUsuario();
            usuario.setSenha(passwordEncoder.encode("091812"));

            usuarioRepository.save(usuario);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                usuario.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @AfterEach
        void limparSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void deveDeletarUsuarioComSucesso() {

            UsuarioDeletarDTO dto = new UsuarioDeletarDTO(
                "091812"
            );

            usuarioService.deletarUsuario(dto);

            assertThat(usuarioRepository.findByEmail(usuario.getEmail())).isEmpty();
        }

        @Test
        void deveLancarExcecaoAoDigitarSenhaIncorreta() {

            UsuarioDeletarDTO dto = new UsuarioDeletarDTO("091814");

            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> usuarioService.deletarUsuario(dto)
            );

            assertThat(exception.getMessage()).isEqualTo("Senha incorreta!");
        }

    }

}
