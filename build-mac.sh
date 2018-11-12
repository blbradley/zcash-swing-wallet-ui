git clone https://github.com/Jorl17/jar2app
cd jar2app
chmod +x install.sh uninstall.sh
sudo ./install.sh

cd -
jar2app build/jars/ZcashSwingWalletUI.jar -i src/resources/images/zcash.icns

wget https://github.com/kozyilmaz/zcash-apple/releases/download/v2.0.1a/zcash-macos-v2.0.1a.tar.bz2
wget https://github.com/kozyilmaz/zcash-apple/releases/download/v2.0.1a/zcash-macos-v2.0.1a.tar.bz2.hash

shasum -a 256 -c zcash-macos-v2.0.1a.tar.bz2.hash
tar -xvf zcash-macos-v2.0.1a.tar.bz2

cp zcash-macos-v2.0.1a/usr/local/bin/zcash* ZCashSwingWalletUI.app/Contents
