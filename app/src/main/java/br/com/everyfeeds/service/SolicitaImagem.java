package br.com.everyfeeds.service;


import android.app.IntentService;
import android.content.Intent;

import br.com.everyfeeds.entity.Canal;

public class SolicitaImagem extends IntentService {

    private static final String DADOS_CANAL= "dadosCanal";
    private  Canal dadosCanal= null;

    public SolicitaImagem(String name) {
        super("ServicoSolicitaImagem");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dadosCanal = (Canal)intent.getSerializableExtra(DADOS_CANAL);
    }
}
