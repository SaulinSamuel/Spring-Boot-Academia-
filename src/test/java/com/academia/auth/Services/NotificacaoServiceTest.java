package com.academia.auth.Services;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.academia.auth.Mappers.NotificacaoMapper;
import com.academia.auth.Repositories.NotificacaoRepository;
import com.academia.auth.Services.auth.UsuarioAutenticadoService;

@ExtendWith(MockitoExtension.class)
public class NotificacaoServiceTest {
    
    @Mock 
    private NotificacaoRepository notificacaoRepository;

    @Mock 
    private UsuarioAutenticadoService usuarioLogado;
    
    @Mock
    private NotificacaoMapper notificacaoMapper;

    @InjectMocks
    private NotificacaoService notificacaoService;

    

}
