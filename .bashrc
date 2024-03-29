# some ls aliases
alias ll='ls -alF'
alias la='ls -A'
alias l='ls -CF'

alias k='kubectl'
alias kn='kubectl get nodes'
alias kp='kubectl get pods'
alias ks='kubectl get services'
alias kpa='kubectl get pods --all-namespaces'

alias cli='./bampli.sh client'
alias cli-def='./bampli.sh client -i /bampli/gremlin/default.groovy'
alias cli-new='./bampli.sh client -i /bampli/gremlin/bampli.groovy'
alias cli-air='./bampli.sh client -i /bampli/gremlin/janus-inmemory.groovy'
alias cli-d='./bampli.sh client -i /bampli/gremlin/describe.groovy'

alias ej='docker-compose -f elassandra/docker-compose.yml'
alias dsu='docker-compose -f datastax/docker-compose.yml -f datastax/studio.yml up -d --scale node=0'
alias dsd='docker-compose -f datastax/docker-compose.yml -f datastax/studio.yml down'
alias di='docker inspect seed | FINDSTR "IPAddress"'

alias dps='docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Image}}\t{{.Status}}"'

alias hp='docker run --interactive --tty --rm --user $UID --volume $(pwd):/app hiptest/hiptest-publisher  "$@"'

alias noconv='export MSYS_NO_PATHCONV=1'
alias noconv0='export MSYS_NO_PATHCONV=0'

#export KUBECONFIG=k3s.yaml