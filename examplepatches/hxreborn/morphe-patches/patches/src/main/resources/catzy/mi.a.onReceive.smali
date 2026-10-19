    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v0
    sget-object v1, Lcom/nieruo/healthapp/manager/h;->g0:Lfe/d;
    if-eqz v0, :cond_60
    invoke-virtual {v0}, Ljava/lang/String;->hashCode()I
    move-result v2
    sparse-switch v2, :sswitch_data_94
    goto :goto_60
    :sswitch_10
    const-string v2, "android.intent.action.DATE_CHANGED"
    invoke-virtual {v0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_19
    goto :goto_60
    :cond_19
    invoke-virtual {v1}, Lfe/d;->s()Lcom/nieruo/healthapp/manager/h;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/h;->d()V
    invoke-virtual {p0, p1}, Lmi/a;->a(Landroid/content/Context;)V
    goto :goto_79
    :sswitch_24
    const-string v2, "android.intent.action.TIME_SET"
    invoke-virtual {v0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_2d
    goto :goto_60
    :cond_2d
    invoke-virtual {v1}, Lfe/d;->s()Lcom/nieruo/healthapp/manager/h;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/h;->d()V
    invoke-virtual {p0, p1}, Lmi/a;->a(Landroid/content/Context;)V
    goto :goto_79
    :sswitch_38
    const-string v2, "android.intent.action.TIMEZONE_CHANGED"
    invoke-virtual {v0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_41
    goto :goto_60
    :cond_41
    invoke-virtual {v1}, Lfe/d;->s()Lcom/nieruo/healthapp/manager/h;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/h;->d()V
    invoke-virtual {p0, p1}, Lmi/a;->a(Landroid/content/Context;)V
    goto :goto_79
    :sswitch_4c
    const-string v2, "android.intent.action.TIME_TICK"
    invoke-virtual {v0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_55
    goto :goto_60
    :cond_55
    invoke-virtual {v1}, Lfe/d;->s()Lcom/nieruo/healthapp/manager/h;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/h;->d()V
    invoke-virtual {p0, p1}, Lmi/a;->a(Landroid/content/Context;)V
    goto :goto_79
    :cond_60
    :goto_60
    invoke-virtual {p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v0
    if-eqz v0, :cond_79
    const-string v2, "key_show_sleep_alarm"
    invoke-virtual {v0, v2}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;)Z
    move-result v0
    const/4 v2, 0x1
    if-ne v0, v2, :cond_79
    invoke-virtual {v1}, Lfe/d;->s()Lcom/nieruo/healthapp/manager/h;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/h;->d()V
    invoke-virtual {p0, p1}, Lmi/a;->a(Landroid/content/Context;)V
    :cond_79
    :goto_79
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    invoke-virtual {p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object p1
    sget-object p2, Leh/a;->b:Leh/d;
    invoke-virtual {p2}, Leh/d;->a()Leh/a;
    move-result-object p2
    const-class v0, Lcom/nieruo/healthapp/ui/activity/SleepAlarmActivity;
    invoke-virtual {p2, v0}, Leh/a;->e(Ljava/lang/Class;)Landroid/app/Activity;
    move-result-object p2
    invoke-static {p1}, Ljava/util/Objects;->toString(Ljava/lang/Object;)Ljava/lang/String;
    invoke-static {p2}, Ljava/util/Objects;->toString(Ljava/lang/Object;)Ljava/lang/String;
    return-void
    nop
    :sswitch_data_94
    .sparse-switch
        -0x5a2f0b56 -> :sswitch_4c
        0x1df32313 -> :sswitch_38
        0x1e1f7f95 -> :sswitch_24
        0x3e117848 -> :sswitch_10
    .end sparse-switch
