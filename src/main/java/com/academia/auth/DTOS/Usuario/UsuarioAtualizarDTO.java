package com.academia.auth.DTOS.Usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioAtualizarDTO {
    
    private String nome;

    private String email;

    @Pattern(
        regexp = "^\\(\\d{2}\\) \\d{5}-\\d{4}$",
        message = "Telefone deve estar no formato (98) 99999-9999!"
    )
    private String telefone;

    @NotBlank(message = "A senha atual é obrigatória!")
    private String senhaAtual;

    private String senhaNova;

}
