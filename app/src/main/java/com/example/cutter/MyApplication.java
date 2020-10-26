package com.example.cutter;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;
import org.acra.config.ConfigurationBuilder;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
@AcraMailSender(mailTo = "OrangeDev8@gmail.com")
@AcraCore(reportContent = { ReportField.APP_VERSION_CODE, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT })
public class MyApplication extends android.app.Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        builder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.JSON);
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class).setEnabled(true).setResIcon(R.drawable.ic_baseline_oval_24)
                .setTitle(getResources().getString(R.string.acra_notification_tittle))
                .setText(getResources().getString(R.string.acra_notification_text))
                .setSendButtonText(getResources().getString(R.string.acra_send_button_text))
                .setDiscardButtonText(getResources().getString(R.string.acra_discard_button_text))
                .setChannelName(getResources().getString(R.string.acra_chanel_name))
                .setResChannelImportance(NotificationManager.IMPORTANCE_HIGH);
        ACRA.init(this, builder);
    }
}
