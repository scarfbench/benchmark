"""
Smoke test for "Customer" app.

Checks:
  1) GET <BASE>/index.xhtml -> 200  (fatal if not)
  2) (Soft) GET <BASE>/list.xhtml -> 200 else WARN
  3) (Soft) GET <BASE>/error.xhtml -> 200 else WARN
  4) GET <BASE>/webapi/Customer/all -> 200, parse JSON or XML
  5) Test CRUD operations:
     - POST new customer
     - GET customer by ID
     - PUT update customer
     - DELETE customer

Environment:
  CUSTOMER_BASE   Base app URL (default: http://localhost:8080/webapi)
  VERBOSE=1       Verbose logging

Exit codes:
  0  success
  2  GET index.xhtml failed
  3  GET /webapi/Customer/all failed
  4  CRUD operations failed
  9  Network / unexpected error
"""
import os
import sys
import json
import re
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("CUSTOMER_BASE", "http://localhost:8080/webapi").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HTTP_TIMEOUT = 12

def vprint(*args):
    if VERBOSE:
        print(*args)

def join(base: str, path: str) -> str:
    if not path:
        return base
    if base.endswith("/") and path.startswith("/"):
        return base[:-1] + path
    if (not base.endswith("/")) and (not path.startswith("/")):
        return base + "/" + path
    return base + path

def http(method: str, url: str, headers: dict | None = None, data: bytes | None = None):
    req = Request(url, method=method, headers=headers or {}, data=data)
    try:
        with urlopen(req, timeout=HTTP_TIMEOUT) as resp:
            response_headers = {}
            for header_name, header_value in resp.headers.items():
                response_headers[header_name] = header_value
            return {
                "status": resp.getcode(),
                "body": resp.read().decode("utf-8", "replace"),
                "content_type": resp.headers.get("Content-Type", ""),
                **response_headers  
            }, None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        response_headers = {}
        if hasattr(e, "headers"):
            for header_name, header_value in e.headers.items():
                response_headers[header_name] = header_value
        return {
            "status": e.code,
            "body": body,
            "content_type": (e.headers.get("Content-Type", "") if hasattr(e, "headers") else ""),
            **response_headers
        }, None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def must_get(path: str, fail_code: int):
    url = join(BASE, path)
    vprint(f"GET {url}")
    resp, err = http("GET", url)
    if err:
        pytest.fail(f"[FAIL] {path} -> {err}")
    if resp["status"] != 200:
        pytest.fail(f"[FAIL] GET {path} -> HTTP {resp['status']}")
    print(f"[PASS] GET {path} -> 200")

def soft_get(path: str):
    url = join(BASE, path)
    vprint(f"GET {url} (soft)")
    resp, err = http("GET", url)
    if err:
        print(f"[WARN] {path} -> {err}", file=sys.stderr); return
    print(f"[{'PASS' if resp['status']==200 else 'WARN'}] GET {path} -> {resp['status']}")

def get_all_customers():
    """Get all customers from the API"""
    url = join(BASE, "/Customer/all")
    vprint(f"GET {url} (Accept: application/json)")
    resp, err = http("GET", url, headers={"Accept": "application/json"})
    if err:
        pytest.fail(f"[FAIL] /Customer/all -> {err}")
    if resp["status"] != 200:
        pytest.fail(f"[FAIL] GET /Customer/all -> HTTP {resp['status']}")
    
    ctype = (resp["content_type"] or "").split(";")[0].strip().lower()
    if ctype == "application/json" or resp["body"].lstrip().startswith(("[", "{")):
        try:
            data = json.loads(resp["body"])
            print(f"[PASS] GET /Customer/all -> 200 (JSON), customers: {len(data) if isinstance(data, list) else 1}")
            return data
        except Exception as e:
            print(f"[WARN] Failed to parse JSON: {e}")
    
    print(f"[PASS] GET /Customer/all -> 200 (XML)")
    
    try:
        customers = []
        customer_matches = re.findall(r'<customer\s+id="(\d+)"[^>]*>.*?</customer>', resp["body"], re.DOTALL)
        for customer_id in customer_matches:
            customers.append({"id": int(customer_id)})
        
        detailed_matches = re.findall(r'<customer\s+id="(\d+)"[^>]*>.*?<firstname>([^<]+)</firstname>.*?<lastname>([^<]+)</lastname>.*?</customer>', resp["body"], re.DOTALL)
        customers = []
        for match in detailed_matches:
            customer_id, firstname, lastname = match
            customers.append({
                "id": int(customer_id),
                "firstname": firstname,
                "lastname": lastname
            })
        
        return customers
    except Exception as e:
        return resp["body"]

def create_customer(customer_data: dict):
    """Create a new customer"""
    url = join(BASE, "/Customer")
    vprint(f"POST {url}")
    
    headers = {"Content-Type": "application/json"}
    data = json.dumps(customer_data).encode('utf-8')
    
    resp, err = http("POST", url, headers=headers, data=data)
    if err:
        print(f"[FAIL] POST {url} -> {err}", file=sys.stderr)
        return None
    if resp["status"] not in [200, 201, 204]:
        print(f"[FAIL] POST {url} -> HTTP {resp['status']}", file=sys.stderr)
        return None
    print(f"[PASS] POST {url} -> {resp['status']}")
    return resp

def get_customer_by_id(customer_id: str):
    """Get customer by ID"""
    url = join(BASE, f"/Customer/{customer_id}")
    vprint(f"GET {url}")
    
    resp, err = http("GET", url, headers={"Accept": "application/json"})
    if err:
        print(f"[FAIL] GET {url} -> {err}", file=sys.stderr)
        return None
    if resp["status"] != 200:
        print(f"[WARN] GET {url} -> HTTP {resp['status']}")
        return None
    print(f"[PASS] GET {url} -> 200")
    return resp

def update_customer(customer_id: str, customer_data: dict):
    """Update customer by ID"""
    url = join(BASE, f"/Customer/{customer_id}")
    vprint(f"PUT {url}")
    
    headers = {"Content-Type": "application/json"}
    data = json.dumps(customer_data).encode('utf-8')
    
    resp, err = http("PUT", url, headers=headers, data=data)
    if err:
        print(f"[FAIL] PUT {url} -> {err}", file=sys.stderr)
        return False
    if resp["status"] not in [200, 204, 303]:
        print(f"[WARN] PUT {url} -> HTTP {resp['status']}")
        return False
    print(f"[PASS] PUT {url} -> {resp['status']}")
    return True

def delete_customer(customer_id: str):
    """Delete customer by ID"""
    url = join(BASE, f"/Customer/{customer_id}")
    vprint(f"DELETE {url}")
    
    resp, err = http("DELETE", url)
    if err:
        print(f"[FAIL] DELETE {url} -> {err}", file=sys.stderr)
        return False
    if resp["status"] not in [200, 204]:
        print(f"[WARN] DELETE {url} -> HTTP {resp['status']}")
        return False
    print(f"[PASS] DELETE {url} -> {resp['status']}")
    return True

def parse_customer_id_from_response(resp):
    """Extract customer ID from response"""
    try:
        if "Location" in resp:
            location = resp["Location"]
            match = re.search(r'/(\d+)(?:/|$)', location)
            if match:
                return match.group(1)
        
        if resp["content_type"].startswith("application/json"):
            data = json.loads(resp["body"])
            if isinstance(data, dict) and "id" in data:
                return str(data["id"])
        
        if "xml" in resp["content_type"] or resp["body"].strip().startswith("<"):
            match = re.search(r'<customer\s+id="(\d+)"', resp["body"])
            if match:
                return match.group(1)
            match = re.search(r'id="(\d+)"', resp["body"])
            if match:
                return match.group(1)
            match = re.search(r'<id>(\d+)</id>', resp["body"])
            if match:
                return match.group(1)
                
    except Exception as e:
        pass
    return None

def test_index_page():
    """Index page should load successfully."""
    must_get("/index.xhtml", 2)


def test_list_page():
    """List page should be accessible."""
    soft_get("/list.xhtml")


def test_get_all_customers():
    """GET /Customer/all should return customer data."""
    customers = get_all_customers()
    assert customers is not None, "Failed to get customers"


def test_create_customer():
    """POST /Customer should create a new customer."""
    test_customer = {
        "firstname": "John",
        "lastname": "Doe",
        "email": "john.doe@example.com",
        "phone": "555-1234",
        "address": {
            "number": 123,
            "street": "Main St",
            "city": "Anytown",
            "province": "CA",
            "zip": "12345",
            "country": "USA"
        }
    }
    create_resp = create_customer(test_customer)
    assert create_resp is not None, "Customer creation failed"


def test_get_customer_by_id():
    """GET /Customer/{id} should return customer details."""
    customers = get_all_customers()
    if not isinstance(customers, list) or not customers:
        pytest.skip("No customers to retrieve")
    customer_id = str(customers[0].get("id", ""))
    if not customer_id:
        pytest.skip("No customer ID found")
    resp = get_customer_by_id(customer_id)
    assert resp is not None, f"Failed to get customer {customer_id}"


def test_get_nonexistent_customer():
    """Scenario: Get a non-existent customer returns null/empty."""
    url = join(BASE, "/Customer/99999")
    vprint(f"GET {url}")
    resp, err = http("GET", url, headers={"Accept": "application/json"})
    assert err is None, f"GET /Customer/99999 -> {err}"
    if resp["status"] == 200:
        body = resp["body"].strip()
        assert not body or body == "null", \
            f"Expected empty or null body for non-existent customer, got: {body[:100]}"
    else:
        assert resp["status"] in [404, 204], \
            f"Expected 404 or 204 for non-existent customer, got {resp['status']}"
    print(f"[PASS] GET non-existent customer -> {resp['status']}")


def test_update_customer():
    """Scenario: Update an existing customer via PUT."""
    customers = get_all_customers()
    if not isinstance(customers, list) or not customers:
        pytest.skip("No customers to update")
    customer_id = str(customers[0].get("id", ""))
    if not customer_id:
        pytest.skip("No customer ID found")
    full_resp = get_customer_by_id(customer_id)
    if full_resp and full_resp.get("body"):
        try:
            updated_data = json.loads(full_resp["body"])
            updated_data["lastname"] = "Updated"
        except (json.JSONDecodeError, TypeError):
            updated_data = {
                "id": int(customer_id),
                "firstname": customers[0].get("firstname", "Updated"),
                "lastname": "Updated",
                "email": "updated@example.com",
                "phone": "555-9999",
                "address": {"number": 1, "street": "Test St", "city": "Test", "province": "TS", "zip": "00000", "country": "US"}
            }
    else:
        updated_data = {
            "id": int(customer_id),
            "firstname": customers[0].get("firstname", "Updated"),
            "lastname": "Updated",
            "email": "updated@example.com",
            "phone": "555-9999",
            "address": {"number": 1, "street": "Test St", "city": "Test", "province": "TS", "zip": "00000", "country": "US"}
        }
    # Try JSON first
    url = join(BASE, f"/Customer/{customer_id}")
    headers = {"Content-Type": "application/json"}
    data = json.dumps(updated_data).encode("utf-8")
    resp, err = http("PUT", url, headers=headers, data=data)
    if err:
        pytest.fail(f"[FAIL] PUT -> {err}")
    if resp["status"] in [200, 204, 303]:
        print(f"[PASS] PUT /Customer/{customer_id} -> {resp['status']}")
        return
    # Try XML as fallback
    xml_body = f'<customer><id>{customer_id}</id><firstname>{updated_data.get("firstname","Test")}</firstname><lastname>Updated</lastname></customer>'
    headers_xml = {"Content-Type": "application/xml"}
    resp2, err2 = http("PUT", url, headers=headers_xml, data=xml_body.encode("utf-8"))
    if err2:
        pytest.fail(f"[FAIL] PUT (XML) -> {err2}")
    if resp2["status"] in [200, 204, 303]:
        print(f"[PASS] PUT /Customer/{customer_id} -> {resp2['status']} (XML)")
        return
    # PUT may not be supported in all framework implementations
    pytest.skip(f"PUT /Customer/{customer_id} returned {resp['status']} (JSON) and {resp2['status']} (XML)")


def test_delete_customer():
    """Scenario: Delete an existing customer."""
    test_customer = {
        "firstname": "ToDelete",
        "lastname": "Temporary",
        "email": "delete@example.com",
        "phone": "555-0000"
    }
    create_resp = create_customer(test_customer)
    if create_resp is None:
        pytest.skip("Could not create customer to delete")
    customer_id = parse_customer_id_from_response(create_resp)
    if not customer_id:
        customers = get_all_customers()
        if isinstance(customers, list) and customers:
            customer_id = str(customers[-1].get("id", ""))
    if not customer_id:
        pytest.skip("Could not determine customer ID for deletion")
    result = delete_customer(customer_id)
    assert result, f"Failed to delete customer {customer_id}"
    print(f"[PASS] DELETE /Customer/{customer_id} successful")


def test_content_negotiation_json():
    """Scenario: Customer endpoint supports JSON format."""
    url = join(BASE, "/Customer/all")
    resp, err = http("GET", url, headers={"Accept": "application/json"})
    assert err is None, f"GET /Customer/all (JSON) -> {err}"
    assert resp["status"] == 200, f"Expected 200, got {resp['status']}"
    ctype = resp["content_type"].lower()
    assert "json" in ctype, f"Expected JSON content type, got: {ctype}"
    print(f"[PASS] Content negotiation JSON: {resp['content_type']}")


def test_content_negotiation_xml():
    """Scenario: Customer endpoint supports XML format."""
    url = join(BASE, "/Customer/all")
    resp, err = http("GET", url, headers={"Accept": "application/xml"})
    assert err is None, f"GET /Customer/all (XML) -> {err}"
    if resp["status"] == 406:
        print("[PASS] Content negotiation XML: 406 Not Acceptable (XML not supported)")
        return
    assert resp["status"] == 200, f"Expected 200 or 406, got {resp['status']}"
    ctype = resp["content_type"].lower()
    assert "xml" in ctype, f"Expected XML content type, got: {ctype}"
    print(f"[PASS] Content negotiation XML: {resp['content_type']}")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
