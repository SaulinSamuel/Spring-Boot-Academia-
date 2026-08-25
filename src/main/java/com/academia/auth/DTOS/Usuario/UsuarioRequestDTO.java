package com.academia.auth.DTOS.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequestDTO {
    
    @NotBlank(message = "Nome é obrigatório!")
    private String nome;

    @NotBlank(message = "Email é obrigatório!")
    @Email
    private String email;

    @NotBlank(message = "Telefone é obrigatório!")
    @Size(max = 20)
    @Pattern(
        regexp = "^\\(\\d{2}\\) \\d{5}-\\d{4}$",
        message = "Telefone deve estar no formato (98) 99999-9999!"
    )
    private String telefone;

    @NotBlank(message = "Senha é obrigatória!")
    private String senha;

}
