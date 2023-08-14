# eclair-cli_autocomplete.sh
# 
# To use this script, source it into your .bashrc
# Use command `source /path/to/eclair-cli_autocomplete.sh`
# This script provides command-line autocompletion for `eclair-cli.kexe`.
# Once sourced in your shell (e.g., Bash), you can start typing a command
# and use the <Tab> key to autocomplete or get suggestions for commands and options.
# Tutorials followed: 
# https://tldp.org/LDP/abs/html/
# https://iridakos.com/programming/2018/03/01/bash-programmable-completion-tutorial
# https://www.youtube.com/watch?v=emhouufDnB4

#!/bin/bash

_eclair_cli() {
    local cur prev words cword
    
    # Initialize the completion environment. 
    # `_init_completion` is a helper function provided by the Bash-completion package.
    _init_completion || return

    local commands="getinfo connect disconnect open rbfopen cpfpbumpfees close forceclose updaterelayfee peers"
    local common_opts="-p --host"
    local connect_opts="--uri --nodeId --address --port"
    local disconnect_opts="--nodeId"
    local open_opts="--nodeId --fundingSatoshis --channelType --pushMsat --fundingFeerateSatByte --announceChannel --openTimeoutSeconds"
    local rbfopen_opts="--channelId --targetFeerateSatByte --lockTime"
    local cpfpbumpfees_opts="--outpoints --targetFeerateSatByte"
    local close_opts="--channelId --shortChannelId --channelIds --shortChannelIds --scriptPubKey --preferredFeerateSatByte --minFeerateSatByte --maxFeerateSatByte"
    local forceclose_opts="--channelId --shortChannelId --channelIds --shortChannelIds"
    local updaterelayfee_opts="--nodeId --nodeIds --feeBaseMsat --feeProportionalMillionths"

	# If the current word starts with a dash (-), it's an option rather than a command
     if [[ ${cur} == -* ]]; then
        local cmd=""
        for ((i=$cword; i>0; i--)); do
            if [[ " ${commands} " == *" ${words[$i]} "* ]]; then
                cmd=${words[$i]}
                break
            fi
        done
        # The loop breaks as soon as the command is assigned to the cmd variable.

	# Depending upon the detected main command, we suggest relevant options.
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
            updaterelayfee)
                COMPREPLY=( $(compgen -W "${updaterelayfee_opts} ${common_opts}" -- ${cur}) )
                ;;
            *)
                COMPREPLY=( $(compgen -W "${common_opts}" -- ${cur}) )
                ;;
        esac
    else
        COMPREPLY=( $(compgen -W "${commands}" -- ${cur}) )
        # If the current word doesn't start with a -, the user is probably typing a main command. Hence, compgen is used to suggest completions from the list of main commands.
    fi
}

complete -F _eclair_cli eclair-cli.kexe

