SUMARY = "Corstone1000 platform Image"
DESCRIPTION = "This is the main image which is the container of all the binaries \
               generated for the Corstone1000 platform."
LICENSE = "MIT"

COMPATIBLE_MACHINE = "corstone1000"

# IMAGE_FSTYPES must be set before 'inherit image'
# https://docs.yoctoproject.org/ref-manual/variables.html#term-IMAGE_FSTYPES
IMAGE_FSTYPES = "wic uefi_capsule"

inherit image
inherit tfm_sign_image
inherit uefi_capsule

DEPENDS += "external-system \
            trusted-firmware-a \
            trusted-firmware-m \
"

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

PACKAGE_INSTALL = ""

UEFI_FIRMWARE_BINARY = "${IMAGE_LINK_NAME}.${CAPSULE_IMGTYPE}"
UEFI_CAPSULE_CONFIG = "${THISDIR}/files/${PN}-capsule-update-image.json"
CAPSULE_IMGTYPE = "wic"

# TF-A settings for signing host images
TFA_BL2_BINARY = "bl2-corstone1000.bin"
TFA_FIP_BINARY = "fip-corstone1000.bin"
TFA_BL2_RE_IMAGE_LOAD_ADDRESS = "0x62353000"
TFA_BL2_RE_SIGN_BIN_SIZE = "0x2d000"
TFA_FIP_RE_IMAGE_LOAD_ADDRESS = "0x68130000"
TFA_FIP_RE_SIGN_BIN_SIZE = "0x00200000"
RE_LAYOUT_WRAPPER_VERSION = "0.0.7"
TFM_SIGN_PRIVATE_KEY = "${libdir}/tfm-scripts/root-RSA-3072_1.pem"
RE_IMAGE_OFFSET = "0x1000"

# Offsets for the .nopt image generation
TFM_OFFSET = "102400"
FIP_OFFSET = "479232"
KERNEL_OFFSET = "2576384"

do_sign_images() {
    # Sign TF-A BL2
    sign_host_image ${RECIPE_SYSROOT}/firmware/${TFA_BL2_BINARY} \
        ${TFA_BL2_RE_IMAGE_LOAD_ADDRESS} ${TFA_BL2_RE_SIGN_BIN_SIZE}

    # Update BL2 in the FIP image
    cp ${RECIPE_SYSROOT}/firmware/${TFA_FIP_BINARY} .
    fiptool update --tb-fw \
        ${TFM_IMAGE_SIGN_DEPLOY_DIR}/signed_${TFA_BL2_BINARY} \
        ${TFM_IMAGE_SIGN_DIR}/${TFA_FIP_BINARY}

    # Sign the FIP image
    sign_host_image ${TFM_IMAGE_SIGN_DIR}/${TFA_FIP_BINARY} \
        ${TFA_FIP_RE_IMAGE_LOAD_ADDRESS} ${TFA_FIP_RE_SIGN_BIN_SIZE}
}
do_sign_images[depends] = "\
    fiptool-native:do_populate_sysroot \
    "

# This .nopt image is not the same as the one which is generated by meta-arm/meta-arm/classes/wic_nopt.bbclass.
# The meta-arm/meta-arm/classes/wic_nopt.bbclass removes the partition table from the wic image, but keeps the
# second bank. This function creates a no-partition image with only the first bank.
create_nopt_image() {
    dd conv=notrunc bs=1 if=${DEPLOY_DIR_IMAGE}/bl2_signed.bin of=${B}/${MACHINE}_image.nopt
    dd conv=notrunc bs=1 if=${DEPLOY_DIR_IMAGE}/tfm_s_signed.bin of=${B}/${MACHINE}_image.nopt seek=${TFM_OFFSET}
    dd conv=notrunc bs=1 if=${DEPLOY_DIR_IMAGE}/signed_fip-corstone1000.bin of=${B}/${MACHINE}_image.nopt seek=${FIP_OFFSET}
    dd conv=notrunc bs=1 if=${DEPLOY_DIR_IMAGE}/Image.gz-initramfs-${MACHINE}.bin of=${B}/${MACHINE}_image.nopt seek=${KERNEL_OFFSET}
}
create_nopt_image[depends] += "mc:firmware:linux-yocto:do_deploy"
do_image_uefi_capsule[prefuncs] += "create_nopt_image"
