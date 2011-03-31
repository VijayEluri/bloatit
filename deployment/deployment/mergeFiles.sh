#!/bin/bash

# Usage / documentation
usage()
{
cat << EOF
usage: $0 -f from -t to 
       $0 -h

This script compare two directories and make merge them. (Like a cp but asking if there are diffs).

OPTIONS:
   -h      Show this message. 
   -f      The directory from where we copy files.
   -t      The directory to where the file are copied.
EOF
}

# Context: Where is this script.
cd "$(dirname $0)"
ROOT=$PWD
cd -
COMMONS=$ROOT/../commons/

# Add the includes 
. $COMMONS/includes.sh

#
# Parsing the arguments
#
FROM=
TO=
while getopts "hf:t:" OPTION
do
    case $OPTION in
        h)
            usage
            exit 1
            ;;
        f)
            FROM=$OPTARG
            ;;
        t)
            TO=$OPTARG
            ;;
        ?)
            usage 1>&2
            exit
            ;;
    esac
done
# Make sure there is no bug in the command line.
if [ -z "$FROM" ] || [ -z "$TO" ] 
then
    error "Arguments are missing !!! \n"
    usage 1>&2
    exit 1
fi
if [ "$FROM" = "$TO" ]
then
    error "The FromDir and ToDir must be different"
    exit 1
fi
if [ ! -e "$1" ] || [ ! -e "$2" ] ; then
    error "Error. Waiting for 2 existing directories. exiting..."
    exit 1
fi

#
# Ask a user what to do with the a unique file to copy.
# OPTIONS:
#     FROM_FILE: the file to copy.
#     TO_FILE: the path where paste the file. (Not the folder, we need the complete path)
performMerge() {
    echo "    $1 and $2 have differences. What do you want to do ?"
    echo "    1 -> keep old ($2)"
    echo "    2 -> use new ($1) and erase $2"
    echo "    3 -> use vimdiff"
    echo "    4 -> merge the files by yourself (exit or ^D to finish)."
    read _reponse
    case $_reponse in
        1)
            info "    keeping $2. Do nothing."
            ;;
        2)
            echo "    cp $1 $2"
            cp "$1" "$2"
            ;;
        3)
            # TODO use modification time to know if merge done.
            vimdiff "$1" "$2"
            echo "    1 -> finish."
            echo "    2 -> Back to menu."
            read _new_reponse
            if [ "$_new_reponse" = "1" ] ; then
                exit
            else
                performMerge "$1" "$2"
            fi
            ;;
        4)
            # TODO use modification time to know if merge done.
            info "    You can use $2.new"
            pushd "$(dirname "$2")" 
            bash -i 
            popd
            echo "    1 -> finish."
            echo "    2 -> Back to menu."
            read _new_reponse
            if [ "$_new_reponse" = "1" ] ; then
                exit
            else
                performMerge "$1" "$2"
            fi
            ;;
        ?)
            performMerge "$1" "$2"
            exit
            ;;
    esac
}

#
# Test if 2 files are the same (based on md5)
# echo false or true.
#
filesEqual(){
    if [ ! -e "$1" ] || [ ! -e "$2" ] ; then
        echo false
        exit 1
    fi
    if [ $(md5sum "$1" | cut -d " " -f 1) = $(md5sum "$2" | cut -d " " -f 1) ] ; then
        echo true
        exit 0
    else
        echo false
        exit 1
    fi
}

#
# Do the work ...
#
tryMerge(){
    local _new_files=( $(find $1 -type f) )
    local _old_files=( ${_new_files[@]/#$1/$2} )

    for (( i = 0; i < ${#_new_files[@]}; i++)) ; do
        local _new_file="${_new_files[$i]}"
        local _old_file="${_old_files[$i]}"
        menu  "Merging [$_new_file] to [$_old_file]"

        if [ -e "$_old_file" ] ; then

            if [ "$(filesEqual "$_old_file" "$_new_file")" = "true" ] ; then
                echo "    No modification in: $_old_file. Do nothing"
            elif [ "$(filesEqual "$_old_file.new" "$_new_file")" = "true" ] ; then
                echo "    No modification since prevous transfer. Do nothing"
            else
                cp "$_new_file" "$_old_file.new"
                performMerge "$_new_file" "$_old_file" 
            fi
        else
            echo "    New file $_new_file is copied to $_old_file."
            if [ ! -e "$(dirname "$_old_file")" ] ; then
                mkdir -p "$(dirname "$_old_file")"
            fi
            cp "$_new_file" "$_old_file" 
            echo "    Customize ? (y/N)"
            read rep
            if [ "$rep" = "y" ] || [ "$rep" = "Y" ] ; then
                vim "$_old_file"
                cp "$_new_file" "$_old_file.new"
            fi
        fi
    done
}

tryMerge "$FROM" "$TO"
success "merge done."

