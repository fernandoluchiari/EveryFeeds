package br.com.everyfeeds.service;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.net.URL;

import br.com.everyfeeds.Principal;
import br.com.everyfeeds.R;
import br.com.everyfeeds.R.string;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Usuario;

public class GeraComponentes extends AsyncTask<Void, Void, Void> {
	private Principal principalActivity;
	private Usuario dadosUsuario;
	private String ERRO_EVERYFEEDS = "Erro EveryFeeds";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

                Usuario dadosFeeds = (Usuario) intent
                        .getSerializableExtra("dadosUsuario");
                dadosUsuario.setCanaisSemana(dadosFeeds.getCanaisSemana());


        }
    };

    public GeraComponentes() {
    }

	public GeraComponentes(Principal principalActivity, Usuario dadosUsuario) {
		super(); 
		this.principalActivity = principalActivity;
		this.dadosUsuario = dadosUsuario;
	}

	@Override
	protected Void doInBackground(Void... params) {
		for(Canal dadosCanalAtual:dadosUsuario.getCanaisSemana()){
            if(dadosCanalAtual
                    .getThumbnails()!= null) {
                dadosCanalAtual.setImagemCanal(getImagemFeed(dadosCanalAtual
                        .getThumbnails()));
            }
		}
		for(Canal dadosCanalOutros:dadosUsuario.getCanaisOutros()) {
            if (dadosCanalOutros
                    .getThumbnails() != null){
                dadosCanalOutros.setImagemCanal(getImagemFeed(dadosCanalOutros
                        .getThumbnails()));
             }
		}
		return null;
	}

	private void gerarTabelaFeedsAntigos() {
		TableLayout tabelaOutrosFeeds = (TableLayout) principalActivity
				.findViewById(R.id.tabelaOutrosFeeds);
		int indice = 0;
		TableRow linhaTabela = null;
		for (Canal dadosCanais : dadosUsuario.getCanaisOutros()) {
			if (indice % 2 == 0) {
				linhaTabela = new TableRow(principalActivity);
				linhaTabela.setPadding(0, 0, 0, 5);
				linhaTabela.setGravity(Gravity.CENTER);
			}
			ImageView imagemFeed = new ImageView(principalActivity);
			imagemFeed.setImageBitmap(dadosCanais.getImagemCanal());
			imagemFeed.setPadding(0, 0, 5, 0);
			linhaTabela.addView(imagemFeed);

			if ((indice == dadosUsuario.getCanaisOutros().size() - 1)
					|| indice % 2 != 0) {
				tabelaOutrosFeeds.addView(linhaTabela);
			}
			indice++;
		}
	}

	private void gerarTabelaFeedsAtuais() {
		TableLayout tabelaFeedsPrincipal = (TableLayout) principalActivity
				.findViewById(R.id.tabelaFeedCorrente);
		for (Canal dadosCanais : dadosUsuario.getCanaisSemana()) {
			TableRow linhaTabela = new TableRow(principalActivity);
			linhaTabela.setPadding(0, 0, 0, 5);
			linhaTabela.setGravity(Gravity.CENTER);
			
			final ImageView imagemFeed = new ImageView(principalActivity);
			imagemFeed.setImageBitmap(dadosCanais.getImagemCanal());
			final String idChannel = dadosCanais.getId();
			/*imagemFeed.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("vnd.youtube://" + idChannel));
					principalActivity.startActivity(intent);
				}
			});*/

			linhaTabela.addView(imagemFeed);

			TextView descricaoFeed = new TextView(principalActivity);
			descricaoFeed.setPadding(5, 0, 0, 0);
			descricaoFeed.setText(dadosCanais.getTitulo());

			linhaTabela.addView(descricaoFeed);
			tabelaFeedsPrincipal.addView(linhaTabela);
		}

	}

	@Override
	protected void onPostExecute(Void result) {
		if (dadosUsuario.getCanaisOutros().isEmpty()
				&& dadosUsuario.getCanaisSemana().isEmpty()) {
			principalActivity.showMessage(principalActivity
					.getString(string.msg_sem_feed));
		} else {
			gerarTabelaFeedsAtuais();
			gerarTabelaFeedsAntigos();
			principalActivity.atualizaComponentes(true);
			principalActivity.showBarraAguarde(false);
		}
		super.onPostExecute(result);
	}
	
	private Bitmap getImagemFeed(String url) {
		Bitmap mIcon11 = null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = 1;
		try {
			URL imageURL = new URL(url);
			mIcon11 = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
		} catch (Exception e) {
			Log.e(ERRO_EVERYFEEDS, e.getMessage());
			e.printStackTrace();
		}
		return mIcon11;


    }

}
