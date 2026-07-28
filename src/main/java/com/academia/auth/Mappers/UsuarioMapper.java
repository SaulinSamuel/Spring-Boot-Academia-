package com.academia.auth.Mappers;

import com.academia.auth.DTOS.Usuario.UsuarioRequestDTO;
import com.academia.auth.DTOS.Usuario.UsuarioResponseDTO;
import com.academia.auth.Models.Usuario;

public class UsuarioMapper {
    
    public static Usuario toEntity(UsuarioRequestDTO dto) {

        Usuario usuario = new Usuario();

        usuario.setEmail(dto.getEmail());
        usuario.setNome(dto.getNome());
        
        return usuario;
    }

    public static UsuarioResponseDTO toDTO(Usuario usuario) {
        
        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.setEmail(usuario.getEmail());
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setDiasAcessoSemana(usuario.getAcessosAcademia().getDiasAcesso());
        dto.setRole(usuario.getRole());

        return dto;
    }
}
