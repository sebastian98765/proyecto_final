package com.hoteluq.pattern.structural;
import com.hoteluq.model.Wallet;
public class WalletPaymentAdapter implements PaymentGateway {private final Wallet wallet;public WalletPaymentAdapter(Wallet wallet){this.wallet=wallet;}public boolean pay(double amount,String ref){wallet.charge(amount,ref);return true;}}
