package bank;


import java.util.Date;
import java.io.Serializable;


public class Transaction
    implements Serializable
{

    public static final int Completed = 1;
    public static final int OverDraft = 2;
    public static final int NothingToDo = 0;
    public static final int NoSuchAccount = 3;


    public double  amount;


    public Account account;


    public int targetId;


    public int status;


    public double getAmount()
    {
	return amount;
    }


    public void setAmount( double amount )
    {
	this.amount = amount;
    }


    public int getTargetId()
    {
	return targetId;
    }


    public void setTargetId( int targetId )
    {
	this.targetId = targetId;
    }


    public int getStatus()
    {
	return status;
    }


    /*
    public Date getDate()
    {
	return new Date();
    }
    */


}
