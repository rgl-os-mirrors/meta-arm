require fvp-envelope.inc

SUMMARY = "Arm Fixed Virtual Platform - Armv8-R Base Architecture Envelope Model FVP"
LIC_FILES_CHKSUM = "file://license_terms/license_agreement.txt;md5=1a33828e132ba71861c11688dbb0bd16 \
                    file://license_terms/third_party_licenses/third_party_licenses.txt;md5=34a1ba318d745f05e6197def68ea5411 \
                    file://license_terms/third_party_licenses/arm_license_management_utilities/third_party_licenses.txt;md5=2e53bda6ff2db4c35d69944b93926c9f"

SRC_URI = "https://developer.arm.com/-/media/Files/downloads/ecosystem-models/${MODEL_CODE}_${PV_URL}_${FVP_ARCH}.tgz;subdir=${BP}"
SRC_URI[sha256sum] = "a7a5ee0b7f3b84a2e98e136a6f3ab648e4b6b5ad365186a397595f3f7c69f558"

MODEL_CODE = "FVP_Base_AEMv8R"