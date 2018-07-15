## [ZENCash](https://zensystem.io/) Desktop GUI Wallet APT repository for Debian/Ubuntu Linux

This is a [ZENCash](https://zensystem.io/) Desktop GUI Wallet made available through a package repository
for Debian/Ubuntu (and similar) Linux systems.

![Screenshot](ZENCashWalletUbuntu.png "Main Window")

**This wallet is targeted at advanced users who understand the implications of running a full Zen node on**
**the local machine, maintaining a full local copy of the blockchain, maintaining and backing up the**
**Zen nodes's `wallet.dat` file etc! The wallet is not suitable for novice crypto-currency users!**

**SECURITY WARNING: Encryption of the wallet.dat file is not yet supported for ZENCash. Using the wallet** 
**on a system infected with malware may result in wallet data/funds being stolen. The**
**wallet.dat needs to be backed up regularly (not just once - e.g. after every 30-40**
**outgoing transactions) and it must also be backed up after creating a new Z address.**

**STABILITY WARNING: The GUI wallet is as yet considered experimental! It is known to exhibit occasional stability problems related to running a full Zen node.**
**Specifically if the locally running `zend` cannot start properly due to issues with the local blockchain, the GUI cannot start either!**
**Users need to be prepared to fix such problems manually as described in the [troubleshooting guide](TroubleshootingGuide.md).**
**Doing so requires command line skills.**

### Installing the ZENCash Desktop GUI Wallet on Linux

To setup the APT repository and install packages, using a terminal run the following commands 
```
sudo apt-get update
sudo apt-get install -y apt-transport-https lsb-release

wget -qO - https://apt.z.cash/zcash.asc | sudo apt-key add -
echo "deb [arch=amd64] https://apt.z.cash/ jessie main" | sudo tee /etc/apt/sources.list.d/zcash.list

wget -qO - http://zcash.coinsmith.co/repo/key.asc | sudo apt-key add -
echo 'deb [arch=amd64] http://zcash.coinsmith.co/repo/ '$(lsb_release -cs)' main' | sudo tee --append /etc/apt/sources.list.d/zcash.list

sudo apt-get update
sudo apt-get install zcash zcash-desktop-gui-wallet
```
Then you need to set up the `zen.conf` configuration file:
```
mkdir -p ~/.zcash
echo "rpcuser=username" >> ~/.zcash/zcash.conf
echo "rpcpassword=$(head -c 32 /dev/urandom | base64)" >> ~/.zcash/zcash.conf
```

Finally you need to download the Z cryptographic keys (takes a while):
```
zen-fetch-params
```
   
### Running the ZENCash Desktop GUI Wallet on Linux

To launch the ZENCash Desktop GUI Wallet you can just search and click on it in the Ubuntu unity menu:
![UnityLauncher](ZENUnityLauncher.png "ZENCash Wallet launcher")

...or alternatively, run the command `zencash-desktop-gui-wallet` from a terminal:
```
zencash-desktop-gui-wallet
```

### Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

### Known issues and limitations
1. Limitation: if two users exchange text messages via the messaging UI TAB and one of them has a system clock, substantially running slow or fast by more than 1 minute, it is possible that this user will see text messages appearing out of order. 
1. Limitation: if a messaging identity has been created (happens on first click on the messaging UI tab), then replacing the `wallet.dat` or changing the node configuration between mainnet and testnet will make the identity invalid. This will result in a wallet update error. To remove the error the directory `~/.ZENCashSwingWalletUI/messaging` may be manually renamed or deleted (when the wallet is stopped). **CAUTION: all messaging history will be lost in this case!**
1. Limitation: Wallet encryption has been temporarily disabled in ZENCash due to stability problems. A corresponding issue 
[#1552](https://github.com/zcash/zcash/issues/1552) has been opened by the ZCash developers. Correspondingly
wallet encryption has been temporarily disabled in the ZENCash Desktop GUI Wallet.
1. Issue: GUI data tables (transactions/addresses etc.) allow copying of data via double click but also allow editing. 
The latter needs to be disabled. 
1. Limitation: The list of transactions does not show all outgoing ones (specifically outgoing Z address 
transactions). A corresponding issue [#1438](https://github.com/zcash/zcash/issues/1438) has been opened 
for the ZCash developers. 
1. Limitation: The CPU percentage shown to be taken by zend on Linux is the average for the entire lifetime 
of the process. This is not very useful. This will be improved in future versions.
