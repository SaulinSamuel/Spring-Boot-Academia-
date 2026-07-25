package com.academia.auth.DTOS.Mensalidade;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensalidadeRequestDTO {
    
    @NotNull(message = "Dias de treino são obrigatórios!")
    private Integer diasTreino;

}
