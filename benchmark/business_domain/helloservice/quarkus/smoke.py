#!/usr/bin/env python3
"""Smoke tests for helloservice-quarkus."""

import os
import sys
import urllib.request

import pytest

HELLO_SERVICE_URL = os.getenv(
    "HELLO_SERVICE_URL",
    "http://localhost:8080/helloservice/HelloServiceBean",
)


def _soap_payload(name: str) -> bytes:
    return f"""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:hel="http://ejb.helloservice.tutorial.jakarta/">
   <soapenv:Header/>
   <soapenv:Body>
      <hel:sayHello>
         <arg0>{name}</arg0>
      </hel:sayHello>
   </soapenv:Body>
</soapenv:Envelope>
""".encode("utf-8")


def _post_soap(name: str):
    request = urllib.request.Request(
        HELLO_SERVICE_URL,
        data=_soap_payload(name),
        headers={"Content-Type": "text/xml; charset=utf-8"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        return (
            response.getcode(),
            response.read().decode("utf-8", errors="replace"),
            response.headers,
        )


def _get_wsdl():
    request = urllib.request.Request(f"{HELLO_SERVICE_URL}?wsdl", method="GET")
    with urllib.request.urlopen(request, timeout=10) as response:
        return (
            response.getcode(),
            response.read().decode("utf-8", errors="replace"),
            response.headers,
        )


@pytest.mark.parametrize("name", ["John", "Jane", "World", "Alice", "Mary Jane"])
def test_named_greetings(name: str):
    _, body, _ = _post_soap(name)
    assert f"Hello, {name}." in body


@pytest.mark.parametrize("name", ["John", "Jane", "Alice", "Bob", "World"])
def test_outline_greetings(name: str):
    _, body, _ = _post_soap(name)
    assert f"Hello, {name}." in body


def test_response_is_non_empty_xml_document():
    _, body, _ = _post_soap("John")
    stripped = body.lstrip()
    assert stripped
    assert stripped.startswith(("<?xml", "<soap", "<SOAP", "<S:", "<env:", "<Envelope"))


def test_response_contains_envelope_and_body():
    _, body, _ = _post_soap("John")
    assert "Envelope" in body
    assert "Body" in body


def test_response_contains_helloservice_namespace():
    _, body, _ = _post_soap("John")
    assert "helloservice" in body


def test_response_does_not_contain_fault():
    _, body, _ = _post_soap("John")
    assert "Fault" not in body


def test_wsdl_is_accessible():
    status, body, _ = _get_wsdl()
    assert status == 200
    assert "wsdl" in body.lower()
    assert "sayHello" in body


def test_valid_request_returns_http_200():
    status, _, _ = _post_soap("John")
    assert status == 200


def test_response_content_type_is_xml():
    _, _, headers = _post_soap("John")
    assert "xml" in headers.get("Content-Type", "").lower()


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
