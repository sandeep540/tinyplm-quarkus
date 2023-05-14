import http from 'k6/http';
import { sleep, check } from 'k6';

export default function () {
  const res = http.get('http://localhost:9090/api/products');
  check(res, {
    'is status 200': (res) => res.status === 200,
    'body size is > 0': (r) => r.body.length > 0,
  });
  sleep(1);
}