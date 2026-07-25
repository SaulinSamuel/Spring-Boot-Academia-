package com.academia.auth.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioAutenticadoService usuarioLogado;

    @Transactional
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {

        if(usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado!");
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);

        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioMapper.toDTO(usuarioSalvo);
    }

    public UsuarioResponseDTO atualizarUsuario(UsuarioAtualizarDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if(!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha incorreta!");
        }

        if(usuarioRepository.existsByEmailAndIdNot(dto.getEmail(), usuario.getId())) {
            throw new BusinessException("Usuário já existente com esse email!");
        }

        usuario.setEmail(dto.getEmail());
        usuario.setNome(dto.getNome());

        if(dto.getSenhaNova() != null) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenhaNova()));
        }
        
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDTO(usuario);
    }

    public Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if(usuario.getRole() != RoleUser.ROLE_ADMIN) {
            throw new BusinessException("Você não tem permissão para ver os usuários!");
        }

        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);

        if(usuarios.isEmpty()) {
            throw new ResourceNotFound("Nenhum usuário encontrado!");
        }

        return usuarios
            .map(UsuarioMapper::toDTO);

    }

    public UsuarioResponseDTO meusDados() {

        Usuario usuario = usuarioLogado.usuarioLogado();

        return UsuarioMapper.toDTO(usuario);
    }

    public void deletarUsuario(UsuarioDeletarDTO dto) {

        Usuario usuario = usuarioLogado.usuarioLogado();

        if(!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha incorreta!");
        }

        usuarioRepository.delete(usuario);
    }

}
