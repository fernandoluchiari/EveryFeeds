package br.com.everyfeeds.service;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import br.com.everyfeeds.Principal;
import br.com.everyfeeds.R;
import br.com.everyfeeds.entity.Canal;
import br.com.everyfeeds.entity.Token;
import br.com.everyfeeds.entity.Usuario;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.api.services.youtube.YouTubeScopes;

public class MainService extends Service implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private NotificationManager myNotificationManager;
	private GoogleApiClient mGoogleApiClient;
	private Token token = new Token();
	private Usuario dadosUsuario = new Usuario();
	private SolicitaToken threadToken;
	private String scopes = "oauth2:" + YouTubeScopes.YOUTUBE;
	private boolean cancelada = false;
	private Calendar dataUltimaConsulta = null;
    private String   PACOTE_EVERYFEEDS = "br.com.everyfeeds";

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null && intent.getBooleanExtra("service",false) == true) {
				Usuario dadosFeeds = (Usuario) intent
						.getSerializableExtra("dadosUsuario");
				dadosUsuario.setCanaisSemana(dadosFeeds.getCanaisSemana());
				verificaFeeds(dadosUsuario.getCanaisSemana());
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i("EveryFeeds-Service", "Servico criado...");

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, new Plus.PlusOptions.Builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		registerReceiver(receiver, new IntentFilter(
				SolicitaCanaisUsuario.NOTIFICATION));
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}

		return (super.onStartCommand(intent, flags, startId));
	}

	private void executaTarefas() {
		threadToken = new SolicitaToken(null, mGoogleApiClient, token, scopes,
				getApplicationContext(), this);
		dataUltimaConsulta = Calendar.getInstance(Locale.ENGLISH);
		threadToken.execute();
	}

	public void executaSubscriptionsBasic() {
		Intent intent = new Intent(this, SolicitaCanaisUsuario.class);
		intent.putExtra("token", token);
		intent.putExtra("service", true);
		startService(intent);
	}

	@Override
	public void onDestroy() {
		Log.i("EveryFeeds-Service", "service cancelado");
		cancelada = true;
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
		}
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	protected void exibeNotificacao(String msg) {
		Intent resultIntent = new Intent(this, Principal.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0,
				resultIntent, 0);
		NotificationCompat.Builder notificacao = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentText(msg)
				.setContentTitle(this.getString(R.string.app_name))
				.setTicker(this.getString(R.string.msg_videoNovo))
				.setDefaults(
						Notification.DEFAULT_LIGHTS
								| Notification.DEFAULT_VIBRATE
								| Notification.DEFAULT_SOUND)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setAutoCancel(true);
		notificacao.setContentIntent(pIntent);
		myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		myNotificationManager.notify(1, notificacao.build());
	}

	public void verificaFeeds(List<Canal> feedsAtuais) {
		if (!isForeground(PACOTE_EVERYFEEDS)) {
			if (feedsAtuais.size() != 0) {
				if (feedsAtuais.size() == 1) {
					exibeNotificacao(this.getString(R.string.msg_existe) + feedsAtuais.size()
							+ this.getString(R.string.msg_notificacao));
				} else {
					exibeNotificacao(this.getString(R.string.msg_existe_plural) + feedsAtuais.size()
							+ this.getString(R.string.msg_notificacao));
				}
			}
		}
	}

	public boolean isForeground(String myPackage) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
				.getRunningTasks(1);

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if (componentInfo.getPackageName().equals(myPackage))
			return true;
		return false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("EveryFeeds-Service", "conectou ao serviço google");
		if (!cancelada) {
			Log.i("EveryFeeds-Service", "Servico iniciado..");
			Log.i("EveryFeeds-Service", "Iniciando requisicao de dados...");
			executaTarefas();
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("EveryFeeds-Service", "suspendeu o serviço google");

	}

}
