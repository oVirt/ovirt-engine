<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="/volume_profile_details">

    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
         <fo:simple-page-master master-name="simple" page-height="29.7cm" page-width="21cm" margin-top="1cm" margin-bottom="2cm" margin-left="2.5cm" margin-right="2.5cm">
           <fo:region-body margin-top="0.5cm"/>
           <fo:region-before extent="0.5cm"/>
           <fo:region-after extent="1.5cm"/>
         </fo:simple-page-master>
       </fo:layout-master-set>

       <fo:page-sequence master-reference="simple">
         <fo:flow flow-name="xsl-region-body">
           <fo:block font-family="Arial" font-size="8pt" font-weight="bold">

                <xsl:if test="./brick_profile_details/brick_profile_detail != ''">
                <xsl:for-each select="./brick_profile_details/brick_profile_detail">
                   <fo:block font="12pt bold Arial">
                        <xsl:value-of select="brick/brick_dir" />
                        <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                   </fo:block>
                   <xsl:for-each select="profile_detail">
                     <xsl:if test="profile_type='CUMULATIVE'">

                        <fo:block font="12pt bold Arial">
                            Block Statistics
                            <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                        </fo:block>

                        <xsl:if test="block_statistic != ''">
                        <fo:table border-width="0.1mm" border-style="solid" table-layout="fixed" width="100%" border-collapse="collapse">
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-header text-align="center">
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Size</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Read Blocks</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Write Blocks</fo:block>
                                </fo:table-cell>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each select="block_statistic">
                                    <fo:table-row border="solid 0.1mm black">
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='block.size']/values/value/datum, ' '), statistic[name/text()='block.size']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="statistic[name/text()='block.bytes.read']/values/value/datum" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="statistic[name/text()='block.bytes.write']/values/value/datum" />
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                        </xsl:if>
                        <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />

                        <xsl:if test="fop_statistic != ''">
                        <fo:block font="12pt bold Arial">
                            Fop Statistics
                            <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                        </fo:block>

                        <fo:table border-width="0.1mm" border-style="solid" table-layout="fixed" width="100%" border-collapse="collapse">
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-header text-align="center">
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">File operation</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">No. of Invocations</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Max-Latency</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Min-Latency</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Avg-Latency</fo:block>
                                </fo:table-cell>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each select="fop_statistic">
                                    <fo:table-row border="solid 0.1mm black">
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="name" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="statistic[name/text()='hits']/values/value/datum" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='min.latency']/values/value/datum, ' '), statistic[name/text()='min.latency']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='max.latency']/values/value/datum, ' '), statistic[name/text()='max.latency']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='avg.latency']/values/value/datum, ' '), statistic[name/text()='avg.latency']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                        </xsl:if>
                        <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
</xsl:if>




                <xsl:if test="./nfs_profile_details/nfs_profile_detail != ''">
                <xsl:for-each select="./nfs_profile_details/nfs_profile_detail">
                   <fo:block font="12pt bold Arial">
                        <xsl:value-of select="nfs_server_ip" />
                        <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                   </fo:block>
                   <xsl:for-each select="profile_detail">
                     <xsl:if test="profile_type='CUMULATIVE'">

                       <xsl:if test="block_statistic != ''">
                        <fo:block font="12pt bold Arial">
                            Block Statistics
                            <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                        </fo:block>

                        <fo:table border-width="0.1mm" border-style="solid" table-layout="fixed" width="100%" border-collapse="collapse">
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-header text-align="center">
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Size</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Read Blocks</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Write Blocks</fo:block>
                                </fo:table-cell>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each select="block_statistic">
                                    <fo:table-row border="solid 0.1mm black">
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='block.size']/values/value/datum, ' '), statistic[name/text()='block.size']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="statistic[name/text()='block.bytes.read']/values/value/datum" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="statistic[name/text()='block.bytes.write']/values/value/datum" />
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                        </xsl:if>
                        <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />

                        <xsl:if test="fop_statistic != ''">
                        <fo:block font="12pt bold Arial">
                            Fop Statistics
                            <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                        </fo:block>

                        <fo:table border-width="0.1mm" border-style="solid" table-layout="fixed" width="100%" border-collapse="collapse">
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-column column-width="3cm"/>
                            <fo:table-header text-align="center">
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">File operation</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">No. of Invocations</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Max-Latency</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Min-Latency</fo:block>
                                </fo:table-cell>
                                <fo:table-cell border="2pt solid black">
                                    <fo:block font-family="Arial" font-size="10pt" font-weight="bold">Avg-Latency</fo:block>
                                </fo:table-cell>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each select="fop_statistic">
                                    <fo:table-row border="solid 0.1mm black">
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="name" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="statistic[name/text()='hits']/values/value/datum" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='min.latency']/values/value/datum, ' '), statistic[name/text()='min.latency']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='max.latency']/values/value/datum, ' '), statistic[name/text()='max.latency']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell text-align="center" border="1pt solid black">
                                            <fo:block>
                                                <xsl:value-of select="concat(concat(statistic[name/text()='avg.latency']/values/value/datum, ' '), statistic[name/text()='avg.latency']/unit)" />
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                        </xsl:if>
                        <fo:block linefeed-treatment="preserve" padding-bottom="10.0pt" font-size="11pt" />
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
</xsl:if>
           </fo:block>
          </fo:flow>
       </fo:page-sequence>


    </fo:root>
  </xsl:template>
</xsl:stylesheet>
