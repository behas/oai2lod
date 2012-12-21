PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

PREFIX fn:	<http://www.w3.org/2005/xpath-functions#>
PREFIX op:	<http://www.w3.org/TR/xquery-operators/#>
PREFIX fx:	<http://www.mediaspaces.info/mapping/functions#>

PREFIX dc:	<http://purl.org/dc/elements/1.1/>
PREFIX onb:	<http://www.mediaspaces.info/schemes/onb#>

CONSTRUCT
{
	?x rdf:type onb:Person .
	?x onb:firstname ?y .
}
WHERE
{
	?x dc:creator ?a.
	?b fx:index-of-string (?a ",") .
	?c op:numeric-add (?b '2'^^xsd:int) .
	?y fn:substring (?a ?c) .
}