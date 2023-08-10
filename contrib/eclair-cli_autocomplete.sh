# To use this script, source it into your .bashrc
# Use command `source /path/to/eclair-cli_autocomplete.sh`


#!/bin/bash

_eclair_cli() {
    local cur prev words cword
    _init_completion || return

    local commands="getinfo connect disconnect open rbfopen cpfpbumpfees close forceclose"
    local common_opts="-p"
    local connect_opts="--uri --nodeId --address --port"
    local disconnect_opts="--nodeId"
    local open_opts="--nodeId --fundingSatoshis --channelType --pushMsat --fundingFeerateSatByte --announceChannel --openTimeoutSeconds"
    local rbfopen_opts="--channelId --targetFeerateSatByte --lockTime"
    local cpfpbumpfees_opts="--outpoints --targetFeerateSatByte"
    local close_opts="--channelId --shortChannelId --channelIds --shortChannelIds --scriptPubKey --preferredFeerateSatByte --minFeerateSatByte --maxFeerateSatByte"
    local forceclose_opts="--channelId --shortChannelId --channelIds --shortChannelIds"

     if [[ ${cur} == -* ]]; then
        local cmd=""
        for ((i=$cword; i>0; i--)); do
            if [[ " ${commands} " == *" ${words[$i]} "* ]]; then
                cmd=${words[$i]}
                break
            fi
        done

        case $cmd in
            connect)
                COMPREPLY=( $(compgen -W "${connect_opts} ${common_opts}" -- ${cur}) )
                ;;
            disconnect)
                COMPREPLY=( $(compgen -W "${disconnect_opts} ${common_opts}" -- ${cur}) )
                ;;
            open)
                COMPREPLY=( $(compgen -W "${open_opts} ${common_opts}" -- ${cur}) )
                ;;
            rbfopen)
                COMPREPLY=( $(compgen -W "${rbfopen_opts} ${common_opts}" -- ${cur}) )
                ;;
            cpfpbumpfees)
                COMPREPLY=( $(compgen -W "${cpfpbumpfees_opts} ${common_opts}" -- ${cur}) )
                ;;
            close)
                COMPREPLY=( $(compgen -W "${close_opts} ${common_opts}" -- ${cur}) )
                ;;
            forceclose)
                COMPREPLY=( $(compgen -W "${forceclose_opts} ${common_opts}" -- ${cur}) )
                ;;
            *)
                COMPREPLY=( $(compgen -W "${common_opts}" -- ${cur}) )
                ;;
        esac
    else
        COMPREPLY=( $(compgen -W "${commands}" -- ${cur}) )
    fi
}

complete -F _eclair_cli eclair-cli.kexe

