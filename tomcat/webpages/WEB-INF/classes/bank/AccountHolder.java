package bank;


import java.io.Serializable;


public class AccountHolder
    implements Serializable
{


    public String name;


    public Account account;


    public String getName()
    {
	return name;
    }


    public void setName( String name )
    {
	this.name = name;
    }


}
