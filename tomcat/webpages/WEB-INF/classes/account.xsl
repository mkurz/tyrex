<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html"/>
  <xsl:template match="account">

    <xsl:variable name="name" select="holder/@name"/>
    <HTML>
      <HEAD>
       <TITLE>
          Intalio Bank: Account Summary for <xsl:value-of select="$name"/>
        </TITLE>
      </HEAD>
      <BODY>
        <IMG src="../exobank.gif" width="200"/>

        <TABLE BORDER="2" CELLSPACING="0" CELLPADDING="4" BORDERCOLOR="#666699">
          <TR>
            <TD BGColor="#666699" align="CENTER">
              <B>
                <Font color="#FFFFFF">
                  <Font size="+1">A</Font>ccount
                  <Font size="+1">S</Font>ummary
                </Font>
              </B>
            </TD>
          </TR>
          <TR>
            <TD>
              <P align="CENTER">
                <xsl:value-of select="$name"/>
                <BR/>
                <xsl:apply-templates select="@date"/>
              </P>

              <TABLE>
                <TR>
                  <TD><B>Account Number:</B></TD>
                  <TD><xsl:value-of select="@id"/></TD>
                </TR>
                <TR>
                  <TD><B>Account Balance:</B></TD>
                  <TD><xsl:call-template name="currencyFormatter">
                        <xsl:with-param name="amount" select="@balance"/>
                      </xsl:call-template>
                  </TD>
                </TR>
            </TABLE>

            </TD>
          </TR>
          <TR>
            <TD Align="CENTER">

              <B>Transaction Information</B>
              <xsl:apply-templates select="transaction"/>
              <xsl:if test="not(transaction)">
                  <P>[None]</P>
              </xsl:if>

            </TD>
          </TR>
          <TR>
            <TD Align="CENTER">

              Super secure online transaction:
              <FORM ACTION="BankServlet">
                <SELECT NAME="txType">
                  <OPTION>Transfer</OPTION>
                </SELECT>
                <INPUT TYPE="HIDDEN" NAME="fromId" value="{@id}"/>
                &#160;<B>Target account:</B>
                <INPUT TYPE="TEXT" SIZE="10" NAME="targetId"/>
                &#160;<B>Amount: $</B><INPUT TYPE="TEXT" NAME="amount" SIZE="8"/>
                &#160;<INPUT TYPE="SUBMIT" VALUE=" Go! "/>
              </FORM>
            </TD>
          </TR>
        </TABLE>
      </BODY>
    </HTML>
  </xsl:template>

  <xsl:template match="holder">
       <xsl:value-of select="@name"/>
  </xsl:template>

  <xsl:template match="transaction">
    <BR/>
    The following transaction was
    <xsl:choose>
       <!-- 1 = Completed, 2 = Overdraft, 3 = Invalid Account, 0 = Nothing to do -->
       <xsl:when test="@status='1'">
          successfully completed.
       </xsl:when>
       <xsl:when test="@status='2'">
          aborted due to overdraft protection. We could have allowed the
          transaction, and then charged you outrageous fees, but we assume
          you don't have enough money to pay them.
       </xsl:when>
       <xsl:when test="@status='3'">
          aborted due to an Invalid Account Number. We could have allowed the
          transaction and used our account number, but we assume
          that you might get slightly irritated if we do so.
       </xsl:when>
       <xsl:otherwise>
        ignored, because there was nothing to do.
       </xsl:otherwise>
    </xsl:choose>
    <TABLE WIDTH="80%" BORDER="1" CELLSPACING="0">
      <TR>
        <TH>TxID</TH>
        <TH>Tx Type</TH>
        <TH>Amount</TH>
        <TH>Target Account</TH>
      </TR>
      <TR>
        <TD><xsl:value-of select="@id"/></TD>
        <TD>Transfer</TD>
        <TD>
             <xsl:call-template name="currencyFormatter">
               <xsl:with-param name="amount" select="@amount"/>
             </xsl:call-template>
        </TD>
        <TD><xsl:value-of select="@target-id"/></TD>
      </TR>
    </TABLE>
  </xsl:template>

  <xsl:template match="@date">
      <xsl:variable name="year"     select="substring-before(.,'-')"/>
      <xsl:variable name="date-Y"   select="substring-after(., '-')"/>
      <xsl:variable name="month"    select="substring-before($date-Y, '-')"/>
      <xsl:variable name="date-Y-M" select="substring-after($date-Y, '-')"/>
      <xsl:choose>
        <xsl:when test="contains($date-Y-M, 'T')">
          <xsl:variable name="day" select="substring-before($date-Y-M, 'T')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="day" select="$date-Y-M"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
         <xsl:when test="$month='01'">January </xsl:when>
         <xsl:otherwise>Error in Month</xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="$day"/>,&#160;<xsl:value-of select="$year"/>
  </xsl:template>

  <xsl:template name="currencyFormatter">
     <xsl:param name="amount">0</xsl:param>
     <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
      <TR>
        <xsl:choose>
          <xsl:when test="contains($amount, '.')">
            <TD>$<xsl:value-of select="substring-before($amount, '.')"/>.</TD>
            <TD VALIGN="TOP">
              <FONT SIZE="-1">
              <xsl:value-of select="substring-after($amount, '.')"/>
              </FONT>
            </TD>
          </xsl:when>
          <xsl:otherwise>
            <TD>$<xsl:value-of select="$amount"/>.</TD>
            <TD VALIGN="TOP">
              <FONT SIZE="-1">00</FONT>
            </TD>
          </xsl:otherwise>
        </xsl:choose>
      </TR>
     </TABLE>
  </xsl:template>

</xsl:stylesheet>
