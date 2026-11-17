package com.hoteluq.pattern.structural;
import com.hoteluq.model.*;import com.hoteluq.model.enums.Role;
public class AdminOperationsProxy implements AdminOperations {private final User actor;private final Wallet wallet;public AdminOperationsProxy(User actor,Wallet wallet){this.actor=actor;this.wallet=wallet;}public void topUp(double amount){if(actor.getRole()!=Role.ADMIN)throw new SecurityException("Solo el administrador puede recargar tarjetas");wallet.topUp(amount,"Recarga autorizada por "+actor.getEmail());}}
