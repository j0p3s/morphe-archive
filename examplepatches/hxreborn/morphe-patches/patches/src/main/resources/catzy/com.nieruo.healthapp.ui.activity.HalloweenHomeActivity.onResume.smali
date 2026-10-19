    invoke-super {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->onResume()V
    const/4 v0, 0x1
    iput-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->r:Z
    const/4 v1, 0x0
    iput-boolean v1, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->v:Z
    iget-boolean v2, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->d:Z
    if-eqz v2, :cond_11
    iput-boolean v1, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->d:Z
    iput-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->s:Z
    :cond_11
    iget-object v1, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->c:Lcom/nieruo/healthapp/utils/g0;
    const-string v2, "halloween_activity_stage"
    const-string v3, "\u672a\u62a5\u540d"
    const-string v4, "\u5df2\u62a5\u540d"
    const-string v5, "\u6253\u5361\u9636\u6bb5"
    const-string v6, "\u5f85\u9886\u53d6"
    const/4 v7, 0x2
    if-eqz v1, :cond_60
    iget-object v8, v1, Lcom/nieruo/healthapp/utils/g0;->q:Landroid/animation/ValueAnimator;
    if-eqz v8, :cond_2b
    invoke-virtual {v8}, Landroid/animation/ValueAnimator;->isRunning()Z
    move-result v8
    if-ne v8, v0, :cond_2b
    goto :goto_45
    :cond_2b
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->k0()V
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->i0()I
    move-result v8
    invoke-virtual {p0, v8}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->o0(I)V
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v8
    check-cast v8, Lcom/nieruo/healthapp/databinding/ActivityHalloweenHomeBinding;
    iget-object v8, v8, Lcom/nieruo/healthapp/databinding/ActivityHalloweenHomeBinding;->a:Landroidx/constraintlayout/widget/ConstraintLayout;
    new-instance v9, Lzc/b;
    invoke-direct {v9, v8, p0, v1}, Lzc/b;-><init>(Landroidx/constraintlayout/widget/ConstraintLayout;Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;Lcom/nieruo/healthapp/utils/g0;)V
    invoke-static {v8, v9}, Landroidx/core/view/a0;->a(Landroid/view/View;Ljava/lang/Runnable;)V
    :goto_45
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->i0()I
    move-result v1
    if-eqz v1, :cond_54
    if-eq v1, v0, :cond_53
    if-eq v1, v7, :cond_51
    move-object v3, v6
    goto :goto_54
    :cond_51
    move-object v3, v5
    goto :goto_54
    :cond_53
    move-object v3, v4
    :cond_54
    :goto_54
    new-instance v0, Lorg/json/JSONObject;
    invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V
    invoke-virtual {v0, v2, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
    invoke-static {v0}, Landroidx/work/impl/v;->W0(Lorg/json/JSONObject;)V
    return-void
    :cond_60
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->q0()Z
    move-result v1
    if-eqz v1, :cond_67
    return-void
    :cond_67
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->p0()V
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;->i0()I
    move-result v1
    if-eqz v1, :cond_79
    if-eq v1, v0, :cond_78
    if-eq v1, v7, :cond_76
    move-object v3, v6
    goto :goto_79
    :cond_76
    move-object v3, v5
    goto :goto_79
    :cond_78
    move-object v3, v4
    :cond_79
    :goto_79
    new-instance v0, Lorg/json/JSONObject;
    invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V
    invoke-virtual {v0, v2, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
    invoke-static {v0}, Landroidx/work/impl/v;->W0(Lorg/json/JSONObject;)V
    return-void
