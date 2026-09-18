package com.academia.auth.Events;

import java.time.LocalDate;

public record AulaCanceladaEvent(
    Long aulaId,
    String nomeAula,
    LocalDate dataAula
) {
    
}
