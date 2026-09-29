#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080/helloservice/HelloServiceBean}"

echo "WSDL check: ${BASE_URL}?wsdl"
WSDL_STATUS=$(curl --max-time 10 -sL -o /dev/null -w "%{http_code}" "${BASE_URL}?wsdl")

echo "SOAP check: ${BASE_URL}"
SOAP_REQUEST='<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns:sayHello xmlns:ns="http://ejb.helloservice.tutorial.jakarta/">
      <arg0>ScarfBench</arg0>
    </ns:sayHello>
  </soap:Body>
</soap:Envelope>'
SOAP_RESPONSE=$(curl --max-time 10 -fsS -H "Content-Type: text/xml;charset=UTF-8" --data-binary "$SOAP_REQUEST" "$BASE_URL")

if [ "$WSDL_STATUS" -eq 200 ] && [[ "$SOAP_RESPONSE" == *"Hello, ScarfBench."* ]]; then
  echo "PASS - WSDL and SOAP sayHello response"
  exit 0
else
  echo "FAIL - WSDL HTTP ${WSDL_STATUS} or unexpected SOAP response"
  exit 1
fi
