package com.academia.auth.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.auth.DTOS.Usuario.UsuarioAtualizarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioDeletarDTO;
import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Exceptions.BusinessException;
import com.academia.auth.Exceptions.ResourceNotFound;
import com.academia.auth.Mappers.UsuarioMapper;
import com.academia.auth.Models.Usuario;
import com.academia.auth.Models.enums.RoleUser;
import com.academia.auth.Repositories.UsuarioRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioAutenticadoService usuarioLogado;

    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {

        if(usuarioRepository.existsByEmail(dto.getEmail())) {
            log.warn("Tentativa de cadastro de usuário já cadastrado!");
            throw new BusinessException("Email já cadastrado!");
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);

        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        log.info("Usuário {} cadastrado com sucesso!", usuarioSalvo.getEmail());

        return UsuarioMapper.toDTO(usuarioSalvo);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UsuarioAtualizarDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em atualizar usuário", usuario.getEmail());

        if(!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            log.info("usuário {} informou sua senha incorretamente", usuario.getEmail());
            throw new BusinessException("Senha incorreta!");
        }

        if(usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), usuario.getId())) {
            log.warn("Usuário {} tentou modificar seu email mas já existente!", usuario.getEmail());
            throw new BusinessException("Usuário já existente com esse email!");
        }

        usuario.setEmail(dto.getEmail());
        usuario.setNome(dto.getNome());

        if(dto.getSenhaNova() != null) {
            log.info("Senha do usuário {} atualizada!", usuario.getEmail());
            usuario.setSenha(passwordEncoder.encode(dto.getSenhaNova()));
        }
        
        usuarioRepository.save(usuario);
        log.info("Usuário {} atualizado com sucesso!", usuario.getEmail());

        return UsuarioMapper.toDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO promoverUsuarioAFuncionario(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em promover usuário a funcionário", usuario.getEmail());

        if (usuario.getRole() != RoleUser.ROLE_ADMIN) {
            log.warn("Usuário {} tentou promover usuário {} a funcionário", usuario.getId(), id);
            throw new BusinessException("Você não tem permissão para promover usuário a funcionário!");
        }

        Usuario usuarioPromovido = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Usuário não encontrado!"));
        
        if (usuarioPromovido.getRole() != RoleUser.ROLE_USER) {
            log.warn("Usuário {} tentou promover usuário {} a funcionário");
            throw new BusinessException("Você não tem permissão para promover funcionários!");
        }

        usuarioPromovido.setRole(RoleUser.ROLE_FUNCIONARIO);

        usuarioRepository.save(usuarioPromovido);
        log.info("Usuário {} promovido a funcionário!", usuarioPromovido.getEmail());

        return UsuarioMapper.toDTO(usuarioPromovido);
    }

    @Transactional
    public UsuarioResponseDTO rebaixarFuncionarioAUsuario(Long id) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("usuário {} entrou em rebaixar funcionário a usuário", usuario.getEmail());;

        if (usuario.getRole() != RoleUser.ROLE_ADMIN) {
            log.warn("");
            throw new BusinessException("Você não tem permissão para rebaixar funcionários!");
        }

        Usuario usuarioRebaixado = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFound("Usuário não encontrado!"));

        if (usuarioRebaixado.getRole() != RoleUser.ROLE_FUNCIONARIO) {
            log.warn("Usuário {} tentou rebaixar usuário {} a aluno!", usuario.getEmail(), usuarioRebaixado.getEmail());
            throw new BusinessException("Este usuário não pode ser rebaixado a um aluno!");
        }

        usuarioRebaixado.setRole(RoleUser.ROLE_USER);

        usuarioRepository.save(usuarioRebaixado);
        
        return UsuarioMapper.toDTO(usuarioRebaixado);
    }

    public Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em listar usuários", usuario.getEmail());
    
        if(usuario.getRole() != RoleUser.ROLE_ADMIN) {
            log.warn("usuário {} tentou visualizar todos os usuários", usuario.getEmail());
            throw new BusinessException("Você não tem permissão para ver os usuários!");
        }

        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);

        if(usuarios.isEmpty()) {
            throw new ResourceNotFound("Nenhum usuário encontrado!");
        }

        log.info("Usuário {} listou usuários com sucesso!", usuario.getEmail());
        return usuarios
            .map(UsuarioMapper::toDTO);

    }

    public UsuarioResponseDTO meusDados() {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("usuário {} visualizou seus dados", usuario.getEmail());

        return UsuarioMapper.toDTO(usuario);
    }

    @Transactional
    public void deletarUsuario(UsuarioDeletarDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();
        log.info("Usuário {} entrou em deletar usuário", usuario.getEmail());

        if(!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            log.warn("Usuário {} digitou sua senha incorretamente!", usuario.getEmail());
            throw new BusinessException("Senha incorreta!");
        }

        log.info("Usuário {} deletado", usuario.getEmail());
        usuarioRepository.delete(usuario);
    }

}
