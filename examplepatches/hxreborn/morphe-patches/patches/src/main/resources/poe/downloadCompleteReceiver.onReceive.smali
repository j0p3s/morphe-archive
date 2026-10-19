const-string v0, "extra_download_id"
const-wide/16 v1, -0x1
invoke-virtual {p2, v0, v1, v2}, Landroid/content/Intent;->getLongExtra(Ljava/lang/String;J)J
move-result-wide v0
iget-object v2, p0, Lcom/poe/util/j2;->a:Lkotlin/jvm/internal/z;
iget-object v2, v2, Lkotlin/jvm/internal/z;->element:Ljava/lang/Object;
if-eqz v2, :done
check-cast v2, Ljava/lang/Number;
invoke-virtual {v2}, Ljava/lang/Number;->longValue()J
move-result-wide v3
cmp-long v5, v0, v3
if-nez v5, :done
iget-object v5, p0, Lcom/poe/util/j2;->b:Lcom/poe/util/k2;
invoke-virtual {v5}, Lcom/poe/util/k2;->invoke()Ljava/lang/Object;
iget-object v5, p0, Lcom/poe/util/j2;->c:Lkotlinx/coroutines/m;
invoke-static {v3, v4}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
move-result-object v6
invoke-virtual {v5, v6}, Lkotlinx/coroutines/m;->d(Ljava/lang/Object;)V
:done
return-void
