<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0">
  <xsl:template name="searchForm">
    <form name="searchForm" method="get" action="search.html">
      <table width="95" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td><img src="images/dotTrans.gif" width="20" height="1" border="0"/></td>
          <td><img src="images/dotTrans.gif" width="75" height="1" border="0"/></td>
        </tr>
        
        <tr>
          <td colspan="2" align="center">
            <input type="text" size="8" name="query"/>
          </td>
        </tr>
        <tr>
          <td>
            <img src="images/dotTrans.gif" width="20" height="1" border="0"/>
          </td>
          <td><span class="subMenuOff"><a href="search.html" onClick="document.searchForm.submit(); return false">Search</a></span></td>
        </tr>
      </table>
    </form>
  </xsl:template>
  
</xsl:stylesheet>

