package com.academia.auth.DTOS.Usuario;

import com.academia.auth.Models.enums.RoleUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {
    
    private Long id;

    private String nome;

    private String email;

    private String telefone;

    private RoleUser role;
}
