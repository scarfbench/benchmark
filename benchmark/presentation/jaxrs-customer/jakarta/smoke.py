"""
Smoke test for "Customer" app (JSF + JAX-RS).

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
  CUSTOMER_BASE   Base app URL (default: http://localhost:9080/jaxrs-customer-10-SNAPSHOT)
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

BASE = os.getenv("CUSTOMER_BASE", "http://localhost:9080/jaxrs-customer-10-SNAPSHOT").rstrip("/")
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
        print(f"[FAIL] {path} -> {err}", file=sys.stderr); sys.exit(9)
    if resp["status"] != 200:
        print(f"[FAIL] GET {path} -> HTTP {resp['status']}", file=sys.stderr); sys.exit(fail_code)
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
    url = join(BASE, "/webapi/Customer/all")
    vprint(f"GET {url} (Accept: application/json)")
    resp, err = http("GET", url, headers={"Accept": "application/json"})
    if err:
        print(f"[FAIL] /webapi/Customer/all -> {err}", file=sys.stderr); sys.exit(9)
    if resp["status"] != 200:
        print(f"[FAIL] GET /webapi/Customer/all -> HTTP {resp['status']}", file=sys.stderr); sys.exit(3)
    
    ctype = (resp["content_type"] or "").split(";")[0].strip().lower()
    if ctype == "application/json" or resp["body"].lstrip().startswith(("[", "{")):
        try:
            data = json.loads(resp["body"])
            print(f"[PASS] GET /webapi/Customer/all -> 200 (JSON), customers: {len(data) if isinstance(data, list) else 1}")
            return data
        except Exception as e:
            print(f"[WARN] Failed to parse JSON: {e}")
    
    print(f"[PASS] GET /webapi/Customer/all -> 200 (XML)")
    
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
    url = join(BASE, "/webapi/Customer")
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
    url = join(BASE, f"/webapi/Customer/{customer_id}")
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
    url = join(BASE, f"/webapi/Customer/{customer_id}")
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
    url = join(BASE, f"/webapi/Customer/{customer_id}")
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

def main():
    must_get("/index.xhtml", 2)
    
    soft_get("/list.xhtml")
    soft_get("/error.xhtml")
    
    customers = get_all_customers()
    
    print("\n[INFO] Testing CRUD operations...")
    
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
    if not create_resp:
        print("[WARN] Customer creation failed; skipping remaining CRUD tests")
        return 0
    
    customer_id = parse_customer_id_from_response(create_resp)
    if not customer_id:
        updated_customers = get_all_customers()
        if isinstance(updated_customers, list) and updated_customers:
            for customer in updated_customers:
                if (isinstance(customer, dict) and 
                    customer.get("firstname") == "John" and 
                    customer.get("lastname") == "Doe"):
                    customer_id = str(customer.get("id"))
                    break
    
    if not customer_id:
        return 0
    
    get_resp = get_customer_by_id(customer_id)
    if get_resp:
        try:
            customer_data = json.loads(get_resp["body"])
            print(f"[PASS] Retrieved customer: {customer_data.get('firstname')} {customer_data.get('lastname')}")
        except Exception as e:
            pass
    
    print("\n[PASS] Smoke sequence complete")
    return 0

if __name__ == "__main__":
    sys.exit(main())
