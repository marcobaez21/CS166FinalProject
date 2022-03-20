---------Setup (School Server)----------
1) First use WINSCP to get the folder onto the school server
2) Then running the following

ssh wch132-YOURLABMACHINE
cd /extra/YOURSCHOOLID

(On Another terminal)
scp -r CS166_Project wch132-YOURLABMACHINE:/extra/YOURSCHOOLID

(Back to the orignal terminal)
cd CS166_Project
source ./startPostgreSQL.sh
source ./createPostgreDB.sh
export DB_NAME="YOURSCHOOLID_DB"
cp ./data/*.csv $PGDATA 
source ./sql/scripts/create_db.sh

----------Running----------

mkdir $DIR/../classes
source ./java/scripts/compile.sh

----------Stopping----------
cd ..
source ./stopPostgreDB.sh