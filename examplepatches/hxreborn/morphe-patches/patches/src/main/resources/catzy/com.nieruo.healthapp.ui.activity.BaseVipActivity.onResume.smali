    invoke-super {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->onResume()V
    instance-of v0, p0, Lcom/nieruo/healthapp/ui/activity/HomeActivity;
    if-nez v0, :cond_39
    sget-object v0, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v0}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v1
    invoke-virtual {v1}, Lcom/nieruo/healthapp/manager/a1;->r()Ljava/lang/Integer;
    move-result-object v1
    if-eqz v1, :cond_18
    invoke-virtual {v1}, Ljava/lang/Integer;->intValue()I
    move-result v1
    goto :goto_19
    :cond_18
    const/4 v1, 0x0
    :goto_19
    if-ltz v1, :cond_39
    invoke-direct {p0}, Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;->getHomeViewModel()Lcom/nieruo/healthapp/viewmodel/m0;
    move-result-object v1
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-virtual {v0}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/a1;->r()Ljava/lang/Integer;
    move-result-object v0
    if-eqz v0, :cond_39
    invoke-virtual {v0}, Ljava/lang/Number;->intValue()I
    move-result v0
    new-instance v2, Lcom/nieruo/healthapp/viewmodel/i0;
    const/4 v3, 0x0
    invoke-direct {v2, v0, v1, v3}, Lcom/nieruo/healthapp/viewmodel/i0;-><init>(ILcom/nieruo/healthapp/viewmodel/m0;Lkotlin/coroutines/d;)V
    invoke-virtual {v1, v2}, Lcom/nieruo/healthapp/viewmodel/e;->e(Lkotlin/jvm/functions/Function1;)V
    :cond_39
    return-void
