PREFIX dc:	<http://purl.org/dc/elements/1.1/>
PREFIX xyz:	<http://exmample.com/test#>
PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX fn: <http://www.w3.org/2005/xpath-functions#>

PREFIX onb:	<http://www.mediaspaces.info/schemes/onb#>
PREFIX img:	<http://www.mediaspaces.info/onb/resources/images#>
PREFIX prs:	<http://www.mediaspaces.info/onb/resources/persons#>

CONSTRUCT
{
	?x dc:creator ?y .
}
WHERE
{
	?x onb:authorfirstname ?a .
	?x onb:authorlastname ?b .
	?y fn:concat (?a ", " ?b) .
}