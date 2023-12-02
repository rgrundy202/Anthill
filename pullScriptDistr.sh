#!/bin/bash

# Script used to pull git repos across all servers

# Checks user and changes SSH values
while true; do
    echo "Enter username:"
    read username

    if [ "$username" = "rgrundy" ]; then
        SSH_USER="$username"
        SSH_KEY="/home/rgrundy/rgrundy-keypair.pem"
        break
    elif [ "$username" = "rnovitsk" ]; then
        SSH_USER="$username"
        SSH_KEY="/home/rnovitsk/rnovitsk-keypair.pem"
        break
    else
        echo "Invalid username. Please try again."
    fi
done

echo "Success! SSH keypair of user '$username' was set."

# Iterates through all potential IPs and executes git pull
for ip_address in {54.82.69.8,3.89.231.167,54.204.131.69,54.147.28.130,3.128.204.116,3.135.204.68,54.241.136.136,54.153.41.121,34.217.13.50,52.25.77.13,13.245.150.107,13.246.232.5,18.166.58.233,18.163.35.20,108.137.2.253,108.136.135.181,3.110.118.143,3.111.39.82,15.152.54.178,13.208.251.78,54.180.122.185,54.180.82.204,54.169.70.132,18.136.201.21,3.27.82.43,13.114.101.52,3.112.237.22,3.99.152.205,15.156.205.154,18.192.57.65,3.67.193.200,34.245.77.164,54.216.88.215,13.40.228.246,3.10.51.103,15.161.56.73,15.160.247.27,52.47.75.71,13.39.18.95,51.20.98.34,13.48.48.239,15.185.38.199,157.175.85.120,18.230.184.226,18.228.224.70,51.17.24.10,51.17.168.219,3.28.44.128,3.29.126.32}
do
    echo "Pulling remote repository at IP $ip_address:"
    ssh -i "$SSH_KEY" "$SSH_USER@$ip_address" 'cd p4-final-r-2 && git pull'
    echo "Most recent repo sucessfully pulled at IP $ip_address"

done

clear
echo "All Drones in the Anthill have the latest news from the Queen."
cat << 'EOF'
              ,
      _,-'\   /|   .    .    /`.
  _,-'     \_/_|_  |\   |`. /   `._,--===--.__
 ^       _/"/  " \ : \__|_ /.   ,'    :.  :. .`-._
        // ^   /7 t'""    "`-._/ ,'\   :   :  :  .`.
        Y      L/ )\         ]],'   \  :   :  :   : `.
        |        /  `.n_n_n,','\_    \ ;   ;  ;   ;  _>
        |__    ,'     |  \`-'    `-.__\_______.==---'
       //  `""\\      |   \            \
       \|     |/      /    \            \
                     /     |             `.
                    /      |               ^
                   ^       |
                           ^
Welcome to the Colony!

EOF

sleep 4