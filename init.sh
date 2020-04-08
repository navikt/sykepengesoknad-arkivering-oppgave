export SRVSYFOGSAK_USERNAME=$(cat /secrets/serviceuser/username)
export SRVSYFOGSAK_PASSWORD=$(cat /secrets/serviceuser/password)

if test -f "/var/run/secrets/nais.io/azuread/client_id"; then
  export AAD_SYFOGSAK_CLIENT_ID=$(cat /var/run/secrets/nais.io/azuread/client_id)
  echo "Eksporterer variabel AAD_SYFOGSAK_CLIENT_ID"
fi

if test -f "/var/run/secrets/nais.io/azuread/client_secret"; then
  export AAD_SYFOGSAK_CLIENT_SECRET=$(cat /var/run/secrets/nais.io/azuread/client_secret)
  echo "Eksporterer variabel AAD_SYFOGSAK_CLIENT_SECRET"
fi
