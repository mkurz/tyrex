package bank;


import java.util.Date;
import java.io.Serializable;


public class Account
    implements Serializable
{


    public int id;


    public double balance;


    public AccountHolder holder;


    public Transaction  tx;


    public int getId()
    {
	return id;
    }


    public void setId( int id )
    {
	this.id = id;
    }


    public double getBalance()
    {
	return balance;
    }


    public void setBalance( double balance )
    {
	this.balance = balance;
    }


    public AccountHolder getHolder()
    {
	return holder;
    }


    public void setHodler( AccountHolder holder )
    {
	this.holder = holder;
    }


    /*
    public Date getDate()
    {
	return new Date();
    }
    */


    public Transaction getTransaction()
    {
	return tx;
    }


    public void setTransaction( Transaction tx )
    {
	this.tx = tx;
    }


}
