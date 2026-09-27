"""Upload one review screenshot to an in-app purchase.

usage: upload_iap_screenshot.py <inAppPurchaseV2 id> <png path>
"""
import hashlib
import json
import pathlib
import sys
import urllib.request

sys.path.insert(0, "/Volumes/shit/projects/menkyo_practice/build/testflight-33-20260927")  # asc.py signs the team API key JWT
from asc import call

iap_id, png = sys.argv[1], pathlib.Path(sys.argv[2])
data = png.read_bytes()

existing = call(f"/v2/inAppPurchases/{iap_id}/appStoreReviewScreenshot")
if existing.get("data"):
    print("existing screenshot", existing["data"]["id"], existing["data"]["attributes"].get("assetDeliveryState"))
    deleted = call(f"/v1/inAppPurchaseAppStoreReviewScreenshots/{existing['data']['id']}", "DELETE")
    print("deleted", deleted)

reservation = call("/v1/inAppPurchaseAppStoreReviewScreenshots", "POST", {
    "data": {
        "type": "inAppPurchaseAppStoreReviewScreenshots",
        "attributes": {"fileName": png.name, "fileSize": len(data)},
        "relationships": {"inAppPurchaseV2": {"data": {"type": "inAppPurchases", "id": iap_id}}},
    }
})
if "data" not in reservation:
    print("reservation failed", json.dumps(reservation, ensure_ascii=False)[:800])
    sys.exit(1)
shot_id = reservation["data"]["id"]
for op in reservation["data"]["attributes"]["uploadOperations"]:
    chunk = data[op["offset"]:op["offset"] + op["length"]]
    request = urllib.request.Request(op["url"], method=op["method"], data=chunk,
                                     headers={h["name"]: h["value"] for h in op["requestHeaders"]})
    with urllib.request.urlopen(request, timeout=120) as response:
        print("chunk", op["offset"], op["length"], response.status)

committed = call(f"/v1/inAppPurchaseAppStoreReviewScreenshots/{shot_id}", "PATCH", {
    "data": {
        "type": "inAppPurchaseAppStoreReviewScreenshots",
        "id": shot_id,
        "attributes": {"uploaded": True, "sourceFileChecksum": hashlib.md5(data).hexdigest()},
    }
})
attrs = committed.get("data", {}).get("attributes", {})
print("committed", shot_id, attrs.get("assetDeliveryState"), committed.get("errors"))
