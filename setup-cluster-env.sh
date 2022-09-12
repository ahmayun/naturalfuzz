sudo yum install -y java-devel
curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs
chmod +x cs
echo 'Y' | ./cs setup # asks for Y
rm cs
source ~/.bash_profile
cs update cs
cs install scala:2.11.8 && cs install scalac:2.11.8
git clone git@github.com:SEED-VT/ProvFuzz.git jazzer
# cd jazzer && sbt compile